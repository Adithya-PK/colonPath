---
license: cc-by-nc-sa-4.0
task_categories:
  - image-segmentation
tags:
  - medical
  - histopathology
  - h-and-e
  - nuclei
  - instance-segmentation
  - colon
  - conic
  - lizard
size_categories:
  - 1K<n<10K
---

# CoNIC 2022 (Colon Nuclei Identification and Counting)

Public **training** release of the CoNIC 2022 Challenge: 4,981 non-overlapping
256x256 H&E histopathology patches (colon tissue, 20x, ~0.5 um/pixel) with
nucleus instance segmentation, classification and counting annotations.

> **Faithful-naming note.** This mirror contains the public CoNIC **training
> split only** (4,981 patches). The challenge **test set (~103k nuclei) is
> permanently held out** and is *not* part of this dataset. The patches are a
> re-tiled view of the [Lizard dataset](https://warwick.ac.uk/fac/cross_fac/tia/data/lizard/)
> (whole-image `.mat` annotations cut into non-overlapping 256x256 tiles).

## Composition

- **4,981** RGB patches of 256x256 px, **431,913** annotated nuclei
- **6** nucleus classes + background
- Single `train` split (the only public release; no official fold split)

### Lizard source breakdown (`source` column)

| source    | patches | origin dataset           |
|-----------|---------|--------------------------|
| `crag`    | 2,304   | CRAG                     |
| `dpath`   | 1,799   | DigestPath               |
| `glas`    | 702     | GlaS                     |
| `pannuke` | 112     | PanNuke                  |
| `consep`  | 64      | CoNSeP                   |

## Schema

| Field        | Type                          | Description                                                    |
|--------------|-------------------------------|----------------------------------------------------------------|
| `image`      | PIL RGB 256x256               | H&E patch, uint8                                                |
| `inst_map`   | PIL grayscale uint16 256x256  | Per-patch unique nucleus instance ID (0 = background)          |
| `class_map`  | PIL grayscale uint8 256x256   | Semantic class per pixel (see table below)                     |
| `patch_id`   | int                           | Row index 0..4980 (matches the original .npy ordering)        |
| `patch_info` | str                           | Lizard source image token, e.g. `consep_1-0000`                |
| `source`     | str                           | Lizard source dataset (`crag`/`dpath`/`glas`/`pannuke`/`consep`)|
| `count_*`    | int (x6)                      | Per-class nucleus count in the central 224x224 region          |

### Semantic class encoding (`class_map`)

| Value | Class       |
|-------|-------------|
| 0     | Background  |
| 1     | Neutrophil  |
| 2     | Epithelial  |
| 3     | Lymphocyte  |
| 4     | Plasma      |
| 5     | Eosinophil  |
| 6     | Connective  |

`count_*` columns (`count_neutrophil` ... `count_connective`) follow the same
1..6 ordering and are taken from the challenge `counts.csv` (counted within the
central 224x224 region only, per the official protocol).

## Cross-dataset overlap (leakage warning)

CoNIC is a re-tiled **subset of Lizard**, which is itself assembled from CRAG,
DigestPath, GlaS, PanNuke and CoNSeP. In particular it shares source images
with **[Angelou0516/PanNuke](https://huggingface.co/datasets/Angelou0516/PanNuke)**
(112 patches), as well as GlaS (702), CRAG (2,304), DigestPath (1,799) and
CoNSeP (64). Use the `source` / `patch_info` columns to perform source-image
level deduplication before co-benchmarking against any of these datasets.

## License

CC BY-NC-SA 4.0 (research / non-commercial use). Same license as the original
CoNIC / Lizard release.

## Citation

```bibtex
@article{graham2024conic,
  title={CoNIC Challenge: Pushing the frontiers of nuclear detection, segmentation, classification and counting},
  author={Graham, Simon and Vu, Quoc Dang and Jahanifar, Mostafa and Weigert, Martin and Schmidt, Uwe and Zhang, Wenhua and others},
  journal={Medical Image Analysis},
  volume={92},
  pages={103047},
  year={2024},
  publisher={Elsevier}
}

@article{graham2021conic,
  title={CoNIC: Colon Nuclei Identification and Counting Challenge 2022},
  author={Graham, Simon and Jahanifar, Mostafa and Vu, Quoc Dang and Hadjigeorghiou, Giorgos and Leech, Thomas and Snead, David and Raza, Shan E Ahmed and Minhas, Fayyaz and Rajpoot, Nasir},
  journal={arXiv preprint arXiv:2111.14485},
  year={2021}
}

@inproceedings{graham2021lizard,
  title={Lizard: A large-scale dataset for colonic nuclear instance segmentation and classification},
  author={Graham, Simon and Jahanifar, Mostafa and Azam, Ayesha and Nimir, Mohammed and Tsang, Yee-Wah and Dodd, Katherine and Hero, Emily and Sahota, Harvir and Tank, Atisha and Benes, Ksenija and others},
  booktitle={Proceedings of the IEEE/CVF International Conference on Computer Vision},
  pages={684--693},
  year={2021}
}
```
