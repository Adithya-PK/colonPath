# SMART INDIA HACKATHON 2026 — OFFICIAL PRESENTATION CONTENT
## Problem Statement ID: SIH26215 | Team ID: 23 | Team Name: Pathometrics
## Project: COLONPATH-AI V3 (Multimodal Clinical Decision-Support System)

---

### SLIDE 1: TITLE PAGE
- **Header:** SMART INDIA HACKATHON 2026
- **Problem Statement ID:** SIH26215
- **Problem Statement Title:** AI-Assisted Histopathology System for Colorectal Cancer
- **Theme:** MedTech / BioTech / HealthTech
- **PS Category:** Hardware / Software Hybrid
- **Team ID:** 23
- **Team Name (Registered on portal):** Pathometrics
- **Project Name:** ColonPath-AI V3
- **Core Summary Badge:**
  • Optical Microscopy + USB/OTG Phone Capture
  • Dual Deep Segmentation: HoVer-Net + U-Net
  • Foundation Vision: Phikon-v2 ViT-L/16 (DINOv2)
  • Multimodal Late Fusion: 16D Morphology + 1024D Visual
  • Temperature-Calibrated 9-Class Tissue Prediction
  • Shannon Entropy Uncertainty & OOD Abstention
  • On-Device Native A4 Clinical Decision-Support PDF

---

### SLIDE 2: IDEA OVERVIEW (Problem & Multimodal Solution)
#### Problem:
- **Complex Tissue Architecture:** Colorectal H&E biopsies contain intricate nuclear atypia and glandular destruction requiring meticulous high-magnification scanning.
- **Diagnostic Fatigue & Subjectivity:** Manual cell counting and qualitative grading (e.g. 'mildly enlarged') lead to inter-observer variability and fatigue.
- **Lack of Point-of-Care Quantification:** Standard optical light microscopes do not provide direct mathematical measurements or digital reports.
- **Expensive Scanner Barrier:** Traditional Whole-Slide Scanners cost ,000–,000, creating a barrier for rural/tier-2 clinics.

#### Our Solution (ColonPath-AI V3):
1. **Point-of-Care Image Capture:** Mounts smartphone/USB camera to standard microscope eyepiece via low-cost adapter (-).
2. **Optical Quality QC:** Real-time Laplacian blur variance, contrast, and brightness validation before inference.
3. **Dual-Branch Morphometry:** HoVer-Net extracts 4-class nuclear phenotyping; U-Net segments gland area, perimeter, and circularity.
4. **Multimodal Late Fusion:** Fuses a 16-D morphology vector with a 1024-D Phikon-v2 foundation embedding into MultimodalFusionNet.
5. **Calibrated 9-Class Output:** NCT-CRC-HE-100K taxonomy with temperature scaling (T=1.25) and Shannon entropy uncertainty.
6. **2x2 Priority Triage & PDF:** Ranks quadrants (R_01..R_04) and generates native A4 clinical PDF reports on-device in 35 seconds.
7. **Clinician-in-the-Loop:** Provides objective decision support; pathologist remains the definitive diagnostic authority.

---

### SLIDE 3: TECHNICAL APPROACH & TECHNOLOGY STACK
#### Pipeline Architecture Flow:
1. **Image Acquisition & Optical QC:** H&E slide capture via USB/OTG camera -> Laplacian blur variance (>100) + brightness/contrast verification.
2. **Parallel Deep Computer Vision:**
   • U-Net (ResNet-34): Gland boundaries, area, circularity, aspect ratio.
   • HoVer-Net: Nuclear distance maps -> 4-class phenotyping.
3. **16-D Morphology Vector Extraction:** 8 nuclear metrics + 8 gland metrics standardized into normalized vector m.
4. **Foundation Vision & Late Fusion:**
   • Phikon-v2 ViT-L/16 (DINOv2) generates 1024-D embedding v.
   • MultimodalFusionNet (1040D -> 128D bottleneck -> 9-class logits).
5. **Safety, Consensus & Reporting:**
   • Temperature scaling (T=1.25) + Shannon entropy H(p).
   • Multi-source consensus voting + EvidenceValidator gatekeeper.
   • 7 streaming visual overlays + Native Android A4 PDF generator.

#### Authoritative Technology Stack:
- **Mobile Client:** Android (Kotlin, Jetpack Compose, Material 3, Native PdfDocument)
- **Backend Services:** Python 3.13, FastAPI, PyTorch 2.5
- **Image Processing:** OpenCV, NumPy, SciPy (Macenko / Laplacian)
- **Gland Segmentation:** U-Net (ResNet-34 Backbone, GLaS pretrained)
- **Nuclear Analysis:** HoVer-Net (CoNSeP / PanNuke PyTorch Checkpoint)
- **Foundation Model:** Phikon-v2 (ViT-L/16 via DINOv2 1024D Embeddings)
- **Multimodal Fusion:** ColonPath MultimodalFusionNet (128D Latent)
- **Calibration & Safety:** Temperature Scaling (T=1.25), Shannon Entropy H(p)
- **Validation & Triage:** ColonPath EvidenceValidator + PriorityRegionEngine
- **Data Persistence:** SQLite Database + case_result.json (SHA-256 Hashes)
- **Report Generation:** Android Native PdfDocument Canvas Engine

---

### SLIDE 4: FEASIBILITY, VIABILITY & SYSTEM ARCHITECTURE
#### 1. Low-Cost Hardware Feasibility:
- Uses existing optical light microscopes in any laboratory.
- Connects via standard USB/OTG camera adapter (–).
- Android smartphone provides portable touch interface.
- Total CapEx < ,500 per lab (vs + for WSI scanners).

#### 2. AI & Software Feasibility:
- Optimized PyTorch CPU execution (~33s end-to-end).
- GPU acceleration drops inference to < 3 seconds.
- Fully local hospital intranet operation (Zero mandatory cloud).
- HIPAA & GDPR compliant with SHA-256 audit hashing.

#### 3. Modular & Edge Architecture:
- Decoupled architecture: Headless REST server + Mobile client.
- One edge server supports multiple microscope rooms via Wi-Fi.
- SQLite case persistence prevents data loss on network drops.
- Recomposition-safe Android coroutine lifecycle handling.

#### 4. Reliability & Medical Risk Control:
- Optical QC rejection halts analysis on blurry or folded tissue.
- Shannon entropy (H(p) >= 0.50) triggers mandatory review flags.
- Deterministic EvidenceValidator blocks AI text hallucinations.
- Strictly Clinical Decision Support (Pathologist holds final sign-out).

---

### SLIDE 5: IMPACT, NOVELTY & STAKEHOLDER BENEFITS
#### Stakeholder Benefits:
- **👨‍⚕️ For Pathologists:**
  • Cuts pre-screening time by calculating exact cell & gland counts.
  • 2x2 Spatial Triage queue (R_01..R_04) highlights most atypical quadrant first.
  • Consensus engine flags internal contradictions across tissue layers.
  • Generates audit-ready A4 PDF reports on device with one tap.
- **🏥 For Hospitals & Labs:**
  • >90% reduction in digital pathology deployment expenditure.
  • Integrates with existing microscopes and hospital LIS/HIS via REST.
  • Standardizes objective morphometry grading across all staff.
  • Accelerates biopsy case turnaround from days to hours.
- **🌍 For Rural & Tier-2/3 Centers:**
  • Brings foundation-grade AI to resource-constrained clinics.
  • Works with low-cost  microscopes and smartphone attachments.
  • Instant telepathology PDF export for remote tertiary second opinions.
  • Bridges the critical specialist deficit in underserved regions.

#### Core Project Novelty:
*Unlike black-box vision models or heavy manual desktop tools, ColonPath-AI fuses 1024-D self-supervised visual representations (Phikon-v2) with an explicit 16-D biological morphology descriptor vector into MultimodalFusionNet, verified by deterministic anti-hallucination claim checks and delivered natively on mobile.*

---

### SLIDE 6: RESEARCH CITATIONS & SCIENTIFIC PROVENANCE
#### Established Research & Datasets (E1 & E2):
- **[1] U-Net (Biomedical Segmentation):** Ronneberger, Fischer & Brox, MICCAI 2015. (arXiv:1505.04597)
- **[2] HoVer-Net (Nuclear Segmentation & Classification):** Graham et al., Medical Image Analysis 2019. (DOI:10.1016/j.media.2019.101563)
- **[3] Phikon-v2 (Pathology Foundation Model):** Filiot et al., arXiv:2409.09173 (2024) [DINOv2 ViT-L/16 via Oquab et al., 2023]
- **[4] Confidence Calibration in Deep Networks:** Guo, Pleiss, Sun, Weinberger, ICML 2017.
- **[5] NCT-CRC-HE-100K & CRC-VAL-HE-7K Benchmarks:** Kather et al., 107,180 H&E patches across 9 classes. (Zenodo:10.5281/zenodo.1214456)
- **[6] CoNSeP Dataset (Colorectal Nuclear Phenotypes):** Graham et al., 24,319 annotated nuclei, Warwick University 2019.

#### ColonPath-AI Original Contributions (E3):
- **✅ 16-D Quantitative Morphology Descriptor Vector:** Standardized geometric representation derived from cell/gland contours.
- **✅ MultimodalFusionNet Architecture:** 1040D input -> 128D latent bottleneck -> 9-class calibrated logits.
- **✅ Multi-Source Cross-Evidence Consensus Engine:** Independent voting across visual, nuclear, and glandular branches.
- **✅ Deterministic EvidenceValidator Gatekeeper:** Mathematically verifies claims against numerical tensors before display.
- **✅ 2x2 Spatial Priority Triage Queue (R_01..R_04):** Local quadrant scoring for high-atypia attention prioritization.
- **✅ Zero-Mock Native Android Client & A4 PDF Generator:** Dynamic on-device compilation bound directly to active case tensors.
- **⚠️ Explicit Boundary:** Clinical decision support prototype; not an autonomous diagnostic device.
