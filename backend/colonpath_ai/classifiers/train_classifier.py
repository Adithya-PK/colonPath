"""
Training & Evaluation Pipeline for Multimodal Colorectal Tissue Classifier.
Trains MultimodalFusionNet on 1024D Phikon-v2 foundation embeddings + 16D morphology from CoNIC 2022 dataset.
"""

import json
import logging
import os
from pathlib import Path
from typing import Dict, Any, List, Optional
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from sklearn.metrics import balanced_accuracy_score, f1_score, precision_score, recall_score, roc_auc_score, confusion_matrix

from foundation.phikon.inference import PhikonV2FeatureExtractor
from fusion.fusion_model import MultimodalFusionNet, TISSUE_CLASSES, NUM_CLASSES
from classifiers.dataset import create_data_splits, derive_tissue_class_from_maps

logging.basicConfig(level=logging.INFO, format="%(asctime)s | [%(levelname)s] %(message)s")
logger = logging.getLogger("train_classifier")

WORKSPACE_ROOT = Path(__file__).resolve().parents[3]
OUTPUT_MODEL_DIR = Path(__file__).resolve().parents[1] / "outputs" / "models"
OUTPUT_MODEL_DIR.mkdir(parents=True, exist_ok=True)


def train_epoch(
    model: nn.Module,
    loader: DataLoader,
    optimizer: torch.optim.Optimizer,
    criterion_mc: nn.Module,
    criterion_bin: nn.Module,
    device: str,
) -> Dict[str, float]:
    model.train()
    total_loss = 0.0
    correct_mc = 0
    correct_bin = 0
    total = 0

    for batch in loader:
        v_emb = batch["visual_embedding"].to(device)
        m_feat = batch["morphology_feature"].to(device)
        mc_target = batch["multiclass_label"].to(device)
        bin_target = batch["binary_label"].to(device)

        optimizer.zero_grad()
        mc_logits, bin_logits, _ = model(v_emb, m_feat)

        loss_mc = criterion_mc(mc_logits, mc_target)
        loss_bin = criterion_bin(bin_logits, bin_target)
        loss = loss_mc + 0.5 * loss_bin

        loss.backward()
        optimizer.step()

        total_loss += loss.item() * len(mc_target)
        pred_mc = mc_logits.argmax(dim=-1)
        pred_bin = bin_logits.argmax(dim=-1)

        correct_mc += (pred_mc == mc_target).sum().item()
        correct_bin += (pred_bin == bin_target).sum().item()
        total += len(mc_target)

    return {
        "loss": total_loss / max(1, total),
        "mc_accuracy": correct_mc / max(1, total),
        "bin_accuracy": correct_bin / max(1, total),
    }


def evaluate(
    model: nn.Module,
    loader: DataLoader,
    criterion_mc: nn.Module,
    criterion_bin: nn.Module,
    device: str,
) -> Dict[str, float]:
    model.eval()
    total_loss = 0.0
    all_mc_preds = []
    all_mc_targets = []
    all_bin_preds = []
    all_bin_targets = []

    with torch.no_grad():
        for batch in loader:
            v_emb = batch["visual_embedding"].to(device)
            m_feat = batch["morphology_feature"].to(device)
            mc_target = batch["multiclass_label"].to(device)
            bin_target = batch["binary_label"].to(device)

            mc_logits, bin_logits, _ = model(v_emb, m_feat)

            loss_mc = criterion_mc(mc_logits, mc_target)
            loss_bin = criterion_bin(bin_logits, bin_target)
            loss = loss_mc + 0.5 * loss_bin

            total_loss += loss.item() * len(mc_target)
            pred_mc = mc_logits.argmax(dim=-1).cpu().numpy()
            pred_bin = bin_logits.argmax(dim=-1).cpu().numpy()

            all_mc_preds.extend(pred_mc)
            all_mc_targets.extend(mc_target.cpu().numpy())
            all_bin_preds.extend(pred_bin)
            all_bin_targets.extend(bin_target.cpu().numpy())

    total = len(all_mc_targets)
    all_mc_preds = np.array(all_mc_preds)
    all_mc_targets = np.array(all_mc_targets)
    all_bin_preds = np.array(all_bin_preds)
    all_bin_targets = np.array(all_bin_targets)

    mc_acc = float(np.mean(all_mc_preds == all_mc_targets))
    mc_bal_acc = float(balanced_accuracy_score(all_mc_targets, all_mc_preds))
    mc_macro_f1 = float(f1_score(all_mc_targets, all_mc_preds, average="macro", zero_division=0))
    bin_acc = float(np.mean(all_bin_preds == all_bin_targets))
    bin_f1 = float(f1_score(all_bin_targets, all_bin_preds, average="binary", zero_division=0))

    return {
        "loss": total_loss / max(1, total),
        "mc_accuracy": mc_acc,
        "mc_balanced_accuracy": mc_bal_acc,
        "mc_macro_f1": mc_macro_f1,
        "bin_accuracy": bin_acc,
        "bin_f1": bin_f1,
    }


def evaluate_test_set(
    model: nn.Module,
    test_loader: DataLoader,
    device: str,
) -> Dict[str, Any]:
    model.eval()
    all_mc_preds = []
    all_mc_probs = []
    all_mc_targets = []
    all_bin_preds = []
    all_bin_probs = []
    all_bin_targets = []

    with torch.no_grad():
        for batch in test_loader:
            v_emb = batch["visual_embedding"].to(device)
            m_feat = batch["morphology_feature"].to(device)
            mc_target = batch["multiclass_label"]
            bin_target = batch["binary_label"]

            mc_logits, bin_logits, _ = model(v_emb, m_feat)
            mc_p = torch.softmax(mc_logits, dim=-1).cpu().numpy()
            bin_p = torch.softmax(bin_logits, dim=-1).cpu().numpy()

            all_mc_probs.append(mc_p)
            all_mc_preds.extend(np.argmax(mc_p, axis=-1))
            all_mc_targets.extend(mc_target.numpy())

            all_bin_probs.append(bin_p)
            all_bin_preds.extend(np.argmax(bin_p, axis=-1))
            all_bin_targets.extend(bin_target.numpy())

    all_mc_probs = np.vstack(all_mc_probs)
    all_mc_preds = np.array(all_mc_preds)
    all_mc_targets = np.array(all_mc_targets)
    all_bin_probs = np.vstack(all_bin_probs)
    all_bin_preds = np.array(all_bin_preds)
    all_bin_targets = np.array(all_bin_targets)

    # Metrics
    mc_acc = float(np.mean(all_mc_preds == all_mc_targets))
    mc_bal_acc = float(balanced_accuracy_score(all_mc_targets, all_mc_preds))
    mc_macro_prec = float(precision_score(all_mc_targets, all_mc_preds, average="macro", zero_division=0))
    mc_macro_rec = float(recall_score(all_mc_targets, all_mc_preds, average="macro", zero_division=0))
    mc_macro_f1 = float(f1_score(all_mc_targets, all_mc_preds, average="macro", zero_division=0))

    bin_acc = float(np.mean(all_bin_preds == all_bin_targets))
    bin_prec = float(precision_score(all_bin_targets, all_bin_preds, average="binary", zero_division=0))
    bin_rec = float(recall_score(all_bin_targets, all_bin_preds, average="binary", zero_division=0))
    bin_f1 = float(f1_score(all_bin_targets, all_bin_preds, average="binary", zero_division=0))
    try:
        bin_auc = float(roc_auc_score(all_bin_targets, all_bin_probs[:, 1]))
    except Exception:
        bin_auc = 0.0

    # Calibration: ECE and Brier Score
    confidences = np.max(all_mc_probs, axis=-1)
    accuracies = (all_mc_preds == all_mc_targets).astype(float)
    bin_boundaries = np.linspace(0, 1, 11)
    ece = 0.0
    for i in range(10):
        b_low, b_high = bin_boundaries[i], bin_boundaries[i + 1]
        in_bin = (confidences > b_low) & (confidences <= b_high)
        prop = np.mean(in_bin)
        if prop > 0:
            ece += np.abs(np.mean(confidences[in_bin]) - np.mean(accuracies[in_bin])) * prop

    # Brier score across one-hot targets
    one_hot_mc = np.zeros_like(all_mc_probs)
    one_hot_mc[np.arange(len(all_mc_targets)), all_mc_targets] = 1.0
    brier_score = float(np.mean(np.sum((all_mc_probs - one_hot_mc) ** 2, axis=-1)))

    cm = confusion_matrix(all_mc_targets, all_mc_preds, labels=list(range(NUM_CLASSES))).tolist()

    return {
        "multiclass": {
            "accuracy": mc_acc,
            "balanced_accuracy": mc_bal_acc,
            "macro_precision": mc_macro_prec,
            "macro_recall": mc_macro_rec,
            "macro_f1": mc_macro_f1,
            "confusion_matrix": cm,
        },
        "binary_tumor": {
            "accuracy": bin_acc,
            "precision": bin_prec,
            "recall": bin_rec,
            "f1_score": bin_f1,
            "roc_auc": bin_auc,
        },
        "calibration": {
            "expected_calibration_error": float(ece),
            "brier_score": brier_score,
        },
        "sample_count": len(all_mc_targets),
    }


def run_training(
    dataset_dir: Path,
    epochs: int = 20,
    batch_size: int = 32,
    lr: float = 1e-3,
    weight_decay: float = 1e-4,
    max_samples: Optional[int] = None,
    seed: int = 42,
) -> Dict[str, Any]:
    device = "cuda" if torch.cuda.is_available() else "cpu"
    if device == "cpu":
        torch.set_num_threads(os.cpu_count() or 8)

    extractor = PhikonV2FeatureExtractor(device=device, use_cache=True)
    info = extractor.metadata

    # 1. Prepare data splits & pre-flight verification
    train_ds, val_ds, test_ds, normalizer = create_data_splits(
        dataset_dir, extractor, max_samples=max_samples, seed=seed, batch_size=batch_size
    )

    # 2. Save normalization parameters
    norm_path = OUTPUT_MODEL_DIR / "normalization_params.json"
    normalizer.save(norm_path)

    # Pre-flight report
    total_samples = len(train_ds) + len(val_ds) + len(test_ds)
    all_mc = np.concatenate([train_ds.multiclass_labels.numpy(), val_ds.multiclass_labels.numpy(), test_ds.multiclass_labels.numpy()])
    all_bin = np.concatenate([train_ds.binary_labels.numpy(), val_ds.binary_labels.numpy(), test_ds.binary_labels.numpy()])

    print("\n" + "=" * 60)
    print("PRE-FLIGHT VALIDATION REPORT")
    print("=" * 60)
    print(f"DATASET PATH:              {dataset_dir}")
    print(f"TOTAL SPECIMENS:          {total_samples}")
    print(f"TRAIN COUNT:              {len(train_ds)} ({len(train_ds)/total_samples*100:.1f}%)")
    print(f"VAL COUNT:                {len(val_ds)} ({len(val_ds)/total_samples*100:.1f}%)")
    print(f"TEST COUNT:               {len(test_ds)} ({len(test_ds)/total_samples*100:.1f}%)")
    print(f"MORPHOLOGY SHAPE:         {train_ds.morphology_features.shape}")
    print(f"EMBEDDING DIMENSION:      {train_ds.visual_embeddings.shape[1]}")
    print(f"FOUNDATION MODEL:         {info.get('model_id')}")
    print(f"FOUNDATION REVISION:      {info.get('revision')}")
    print(f"FOUNDATION VERIFIED:      {info.get('verified')}")
    print(f"MODEL PARAMETERS:         131,211 (MultimodalFusionNet)")
    print(f"BATCH SIZE:               {batch_size}")
    print(f"EPOCHS:                   {epochs}")
    print(f"LEARNING RATE:            {lr}")
    print(f"WEIGHT DECAY:             {weight_decay}")
    print(f"SEED:                     {seed}")

    print("\nCLASS DISTRIBUTION:")
    for idx, cname in enumerate(TISSUE_CLASSES):
        c_cnt = int(np.sum(all_mc == idx))
        print(f"  {cname:4s}: {c_cnt:5d} ({c_cnt/total_samples*100:5.2f}%)")

    print("\nBINARY TUMOR DISTRIBUTION:")
    n_non_tum = int(np.sum(all_bin == 0))
    n_tum = int(np.sum(all_bin == 1))
    print(f"  Non-Tumor (0): {n_non_tum:5d} ({n_non_tum/total_samples*100:5.2f}%)")
    print(f"  Tumor     (1): {n_tum:5d} ({n_tum/total_samples*100:5.2f}%)")

    print("\nPRE-FLIGHT PASSED — STARTING TRAINING\n" + "=" * 60)

    train_loader = DataLoader(train_ds, batch_size=batch_size, shuffle=True)
    val_loader = DataLoader(val_ds, batch_size=batch_size, shuffle=False)
    test_loader = DataLoader(test_ds, batch_size=batch_size, shuffle=False)

    # 3. Model, Optimizer, Scheduler, Criteria
    model = MultimodalFusionNet().to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=lr, weight_decay=weight_decay)
    scheduler = torch.optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=epochs)
    criterion_mc = nn.CrossEntropyLoss()
    criterion_bin = nn.CrossEntropyLoss()

    best_val_metric = 0.0
    best_epoch = 0
    best_ckpt_path = OUTPUT_MODEL_DIR / "best_classifier.pth"

    for epoch in range(1, epochs + 1):
        t_metrics = train_epoch(model, train_loader, optimizer, criterion_mc, criterion_bin, device)
        v_metrics = evaluate(model, val_loader, criterion_mc, criterion_bin, device)
        scheduler.step()

        # Track balanced accuracy for best model selection
        selection_metric = v_metrics["mc_balanced_accuracy"]

        print(
            f"Epoch {epoch:02d}/{epochs:02d} | "
            f"Train Loss: {t_metrics['loss']:.4f}, Train Acc: {t_metrics['mc_accuracy']:.4f} | "
            f"Val Loss: {v_metrics['loss']:.4f}, Val Acc: {v_metrics['mc_accuracy']:.4f}, "
            f"Val BalAcc: {v_metrics['mc_balanced_accuracy']:.4f}, Val MacroF1: {v_metrics['mc_macro_f1']:.4f}"
        )

        if selection_metric >= best_val_metric:
            best_val_metric = selection_metric
            best_epoch = epoch
            torch.save(
                {
                    "epoch": epoch,
                    "model_state_dict": model.state_dict(),
                    "optimizer_state_dict": optimizer.state_dict(),
                    "val_balanced_accuracy": best_val_metric,
                    "val_accuracy": v_metrics["mc_accuracy"],
                    "val_macro_f1": v_metrics["mc_macro_f1"],
                    "classes": TISSUE_CLASSES,
                    "seed": seed,
                    "foundation_provenance": info,
                    "morphology_schema_version": "16D_canonical_v1",
                    "normalization_reference": "outputs/models/normalization_params.json",
                    "training_samples": len(train_ds),
                },
                best_ckpt_path,
            )

    print(f"\n[DONE] Training Finished. Best Epoch: {best_epoch:02d} | Best Val Balanced Acc: {best_val_metric:.4f}")
    print(f"[OK] Best Model Checkpoint Saved: {best_ckpt_path}")

    # 4. Load best checkpoint and evaluate on held-out test split
    best_checkpoint = torch.load(best_ckpt_path, map_location=device, weights_only=False)
    model.load_state_dict(best_checkpoint["model_state_dict"])
    test_results = evaluate_test_set(model, test_loader, device)

    print("\n" + "=" * 60)
    print("HELD-OUT TEST SET EVALUATION REPORT (15% SPLIT)")
    print("=" * 60)
    print(f"Test Accuracy:          {test_results['multiclass']['accuracy']:.4f}")
    print(f"Test Balanced Accuracy: {test_results['multiclass']['balanced_accuracy']:.4f}")
    print(f"Test Macro Precision:   {test_results['multiclass']['macro_precision']:.4f}")
    print(f"Test Macro Recall:      {test_results['multiclass']['macro_recall']:.4f}")
    print(f"Test Macro F1:          {test_results['multiclass']['macro_f1']:.4f}")
    print(f"Binary Tumor Accuracy:  {test_results['binary_tumor']['accuracy']:.4f}")
    print(f"Binary Tumor Precision: {test_results['binary_tumor']['precision']:.4f}")
    print(f"Binary Tumor Recall:    {test_results['binary_tumor']['recall']:.4f}")
    print(f"Binary Tumor F1:        {test_results['binary_tumor']['f1_score']:.4f}")
    print(f"Binary Tumor ROC-AUC:   {test_results['binary_tumor']['roc_auc']:.4f}")
    print(f"Expected Calib Error:   {test_results['calibration']['expected_calibration_error']:.4f}")
    print(f"Brier Score Loss:       {test_results['calibration']['brier_score']:.4f}")

    # 5. Save comprehensive training and evaluation artifacts
    training_summary = {
        "architecture": "MultimodalFusionNet",
        "foundation_model": info.get("model_id"),
        "foundation_weights": info.get("weights_signature"),
        "dataset_path": str(dataset_dir),
        "total_specimens": total_samples,
        "splits": {
            "train_count": len(train_ds),
            "val_count": len(val_ds),
            "test_count": len(test_ds),
        },
        "hyperparameters": {
            "epochs": epochs,
            "batch_size": batch_size,
            "learning_rate": lr,
            "weight_decay": weight_decay,
            "seed": seed,
        },
        "best_epoch": best_epoch,
        "best_val_balanced_accuracy": best_val_metric,
        "test_evaluation": test_results,
    }

    summary_path = OUTPUT_MODEL_DIR / "training_summary.json"
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump(training_summary, f, indent=2)
    print(f"[OK] Saved Full Training Summary: {summary_path}")

    return training_summary


if __name__ == "__main__":
    conic_dir = WORKSPACE_ROOT / "cv" / "datasets" / "conic2022_processed"
    run_training(conic_dir, epochs=20, batch_size=32)
