import os
import tempfile
from fastapi import FastAPI, UploadFile, File, Form
from faster_whisper import WhisperModel

app = FastAPI()

MODEL_SIZE = os.getenv("WHISPER_MODEL", "small")
COMPUTE_TYPE = os.getenv("WHISPER_COMPUTE_TYPE", "int8")
DEVICE = os.getenv("WHISPER_DEVICE", "cpu")

model = WhisperModel(MODEL_SIZE, device=DEVICE, compute_type=COMPUTE_TYPE)

@app.get("/health")
def health():
    return {"status": "ok", "model": MODEL_SIZE, "device": DEVICE, "compute_type": COMPUTE_TYPE}

@app.post("/transcribe")
async def transcribe(
        audio: UploadFile = File(...),
        language: str = Form(default="en"),
):
    suffix = os.path.splitext(audio.filename or "")[1] or ".wav"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as f:
        f.write(await audio.read())
        tmp_path = f.name

    try:
        segments, info = model.transcribe(tmp_path, language=language, vad_filter=True)
        segs = []
        parts = []
        last_end = 0.0
        for s in segments:
            segs.append({"start": float(s.start), "end": float(s.end), "text": s.text.strip()})
            if s.text.strip():
                parts.append(s.text.strip())
            last_end = float(s.end)
        return {"text": " ".join(parts).strip(), "language": info.language, "duration": last_end, "segments": segs}
    finally:
        try:
            os.remove(tmp_path)
        except:
            pass