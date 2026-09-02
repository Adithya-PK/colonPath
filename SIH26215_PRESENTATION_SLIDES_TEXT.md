# SMART INDIA HACKATHON 2026 — OFFICIAL PRESENTATION SLIDES CONTENT
## Problem Statement ID: SIH26215 | Team ID: 23 | Team Name: Pathometrics
## Project: COLONPATH-AI V3 (Multimodal Clinical Decision-Support System)

---

### SLIDE 1: TITLE PAGE
- **Problem Statement ID:** SIH26215
- **Problem Statement Title:** AI-Assisted Histopathology System for Colorectal Cancer
- **Theme:** MedTech / BioTech / HealthTech
- **PS Category:** Hardware / Software Hybrid
- **Team ID:** 23
- **Team Name (Registered on portal):** Pathometrics

---

### SLIDE 2: IDEA TITLE

#### Problem
- Colon H&E slides contain complex nuclear and glandular patterns.
- Repetitive counting, measurement and comparison consume valuable expert time.
- Conventional microscopy does not directly provide structured quantitative evidence.
- High-end digital pathology scanners are too expensive for smaller laboratories.

#### Our Solution
- Capture H&E tissue using a microscope + USB camera + smartphone/OTG.
- AI analyzes nuclei and glandular structures simultaneously.
- Extract measurable quantitative morphology:
  1. Nuclear count & cell populations
  2. Nuclear area & perimeter
  3. Shape / eccentricity / circularity
  4. Gland area, perimeter, and aspect ratio
  5. 16-D morphology feature vector
- Perform multimodal late-fusion with pathology foundation embeddings.
- Generate an evidence-grounded, calibrated clinical report and native PDF.
- Pathologist remains the final decision-maker.

---

### SLIDE 3: TECHNICAL APPROACH

#### Left Side (Pipeline Steps):
**1. H&E Image Acquisition**
- H&E-stained colon tissue
- Microscope + USB/UVC camera
- OTG -> Android smartphone
- Optical image quality & blur check

**2. Image Preprocessing**
- Optical quality validation
- Stain/color normalization
- Image tiling & ROI selection

**3. Computer Vision (HoVer-Net + U-Net)**
- HoVer-Net: Nuclear segmentation & 4-class phenotyping
- U-Net: Gland architecture segmentation

**4. Morphology Engine**
- Nuclear count & subtype distribution
- Nuclear area, perimeter & circularity
- Gland area, perimeter & aspect ratio
- 16-D standardized morphology vector

**5. AI Evidence & Fusion Layer**
- Phikon-v2 ViT-L/16 visual embeddings
- MultimodalFusionNet late-fusion (1024D + 16D)
- Temperature calibration & Shannon entropy
- Deterministic EvidenceValidator & native PDF

---

#### Right Side (Technology Stack Table):

| Layer | Technology |
| :--- | :--- |
| **Mobile Interface** | Android / Kotlin / Jetpack Compose |
| **Backend** | Python + FastAPI + PyTorch |
| **Image Processing** | OpenCV + NumPy + SciPy |
| **Gland Analysis** | U-Net (ResNet-34 Backbone) |
| **Nuclear Analysis** | HoVer-Net (CoNSeP Checkpoint) |
| **Morphology & Metrics** | 16-D Feature Vector (NumPy) |
| **Embeddings** | Phikon-v2 (ViT-L/16 via DINOv2) |
| **Multimodal Fusion** | MultimodalFusionNet (Late Fusion) |
| **Calibration & Safety** | Temperature Scaling (T=1.25) + Entropy |
| **AI Evidence** | Deterministic EvidenceValidator |
| **Data Storage** | SQLite / Metadata DB + File Storage |
| **Report Generation** | Android Native PdfDocument |

---

### SLIDE 4: FEASIBILITY AND VIABILITY

#### Low-Cost Hardware
- Existing microscope with compatible USB/UVC camera
- USB OTG for smartphone connectivity
- Android smartphone as the capture/interface device
- No dedicated high-end scanner required (CapEx < \,500)

#### AI & Software Feasibility
- Python + FastAPI backend with optimized PyTorch execution
- OpenCV + NumPy for image processing
- HoVer-Net for nuclear analysis & U-Net for glands
- MultimodalFusionNet for combined prediction
- Fast CPU/GPU execution with offline local intranet support

#### Modular & Scalable Architecture
- Independent modules with defined Input -> Processing -> Output
- Hybrid computation: backend for AI processing, smartphone for interface
- Can be extended to additional organs and future on-device deployment
- SQLite persistence prevents data loss on network drops

#### Reliability & Risk Control
- Optical image-quality rejection for poor/blurry samples
- Shannon entropy (H(p)) and OOD uncertainty detection
- Deterministic validation eliminates AI text hallucinations
- Evidence-grounded AI outputs bound to real tensors
- Pathologist remains in the decision loop

---

### SLIDE 5: IMPACT AND BENEFITS

#### 👨‍⚕️ Pathologists
- Reduces repetitive manual assessment and diagnostic fatigue
- Provides objective quantitative metrics on nuclei and glands
- 2x2 Spatial Region Triage highlights the most atypical quadrant first
- Presents validated findings in a standardized A4 PDF format

#### 🏥 Hospitals & Diagnostic Centres
- Low-cost digital image acquisition utilizing existing microscopes
- Reduces dependency on expensive (\+) digital pathology systems
- Accelerates biopsy turnaround time from days to hours
- Supports a portable, mobile-oriented clinical workflow

#### 🌍 Resource-Constrained Settings
- Smartphone-based image capture and touch interface
- Minimal additional hardware requirements (\–\ adapter)
- Enables deployment in Tier-2/Tier-3 and rural diagnostic centres
- Instant telepathology PDF export for remote specialist consultation

#### Key Impact Banner:
> *Converts complex microscopic observations into measurable and structured evidence while keeping the pathologist at the centre of the final decision.*

---

### SLIDE 6: RESEARCH AND REFERENCES

- **[1] HoVer-Net — Nuclear Segmentation & Classification**  
  Graham et al., *Medical Image Analysis*, 2019.  
  [HoVer-Net Research Paper](https://pubmed.ncbi.nlm.nih.gov/31561183/)

- **[2] U-Net — Biomedical Image Segmentation**  
  Ronneberger, Fischer & Brox, *MICCAI*, 2015.  
  [U-Net Research Paper](https://arxiv.org/abs/1505.04597)

- **[3] Phikon-v2 — Pathology Foundation Vision Model**  
  Filiot et al., *arXiv*, 2024. [DINOv2: Oquab et al., *arXiv*, 2023]  
  [Phikon-v2 Research Paper](https://arxiv.org/abs/2409.09173)

- **[4] Confidence Calibration in Deep Networks**  
  Guo, Pleiss, Sun & Weinberger, *ICML*, 2017.

- **[5] NCT-CRC-HE-100K & CRC-VAL-HE-7K — Colorectal H&E Dataset**  
  Kather et al., 107,180 colorectal histology image patches.  
  [NCT-CRC-HE-100K Dataset](https://zenodo.org/records/1214456)

- **[6] ColonPath-AI Project-Specific Contributions**  
  MultimodalFusionNet, 16-D Morphology Vector, Consensus Engine, EvidenceValidator, and Native Android PDF Generator.
