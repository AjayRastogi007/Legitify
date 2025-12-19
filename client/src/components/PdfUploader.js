import { useState, useRef } from "react";
import { Upload, X, FileText } from "lucide-react";
import { Button } from "./ui/button";
import { useAuth } from "../context/AuthContext";
import { useAlert } from "../context/AlertContext";
import api from "../api/axios";

const MAX_FILE_SIZE = 30 * 1024 * 1024;

const PdfUploader = () => {
  const { auth } = useAuth();
  const isAuthenticated = !!auth.user;
  const { showAlert } = useAlert();

  const [files, setFiles] = useState([]);
  const [isDragging, setIsDragging] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [resultPdfUrl, setResultPdfUrl] = useState(null);

  const inputRef = useRef(null);

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);

    const droppedFile = e.dataTransfer.files?.[0];
    if (!droppedFile) return;

    if (!validateFile(droppedFile)) return;

    setFiles([droppedFile]);
  };

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!validateFile(file)) {
      e.target.value = "";
      return;
    }

    setFiles([file]);
  };

  const removeFile = (index) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUpload = async () => {
    if (!isAuthenticated) {
      showAlert({
        title: "Sign in required",
        message: "Please sign in to continue.",
        type: "warning",
      });
      return;
    }

    if (files.length === 0) {
      showAlert({
        title: "No file selected",
        message: "Please upload a document first.",
        type: "warning",
      });
      return;
    }

    try {
      setIsAnalyzing(true);

      const formData = new FormData();
      formData.append("file", files[0]);

      const res = await api.post("/service/analyze", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const jobId = res.data.jobId;

      const pdfUrl = await pollJobStatus(jobId);

      setResultPdfUrl(pdfUrl);

      showAlert({
        title: "Analysis complete",
        message: "Your document has been analyzed successfully.",
        type: "success",
      });

    } catch (error) {
      showAlert({
        title: "Upload failed",
        message: error.message || "Something went wrong",
        type: "error",
      });
    } finally {
      setIsAnalyzing(false);
    }
  };


  const handleViewPdf = async () => {
    try {
      const fileName = resultPdfUrl.split("/").pop();

      const response = await api.get(
        `/service/pdf/${fileName}`,
        { responseType: "blob" }
      );

      const pdfBlob = new Blob([response.data], {
        type: "application/pdf",
      });

      const pdfUrl = URL.createObjectURL(pdfBlob);

      window.open(pdfUrl, "_blank", "noopener,noreferrer");

      setFiles([]);
      setResultPdfUrl(null);
      if (inputRef.current) {
        inputRef.current.value = "";
      }

    } catch (error) {
      showAlert({
        title: "Failed to open PDF",
        message: "Unable to load the report.",
        type: "error",
      });
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  const validateFile = (file) => {
    if (file.size > MAX_FILE_SIZE) {
      showAlert({
        title: "File too large",
        message: "Maximum allowed file size is 30 MB.",
        type: "warning",
      });
      return false;
    }
    return true;
  };

  const pollJobStatus = async (jobId) => {
    const interval = 2000;
    const timeout = 60000;
    const start = Date.now();

    while (Date.now() - start < timeout) {
      const res = await api.get(`/service/jobs/${jobId}`);

      if (res.data.status === "DONE") {
        return res.data.pdfUrl;
      }

      if (res.data.status === "FAILED") {
        throw new Error(res.data.error || "Analysis failed");
      }

      await new Promise((r) => setTimeout(r, interval));
    }

    throw new Error("Analysis timed out");
  };


  return (
    <div className="pdf-uploader-wrapper w-full max-w-2xl mx-auto px-6 pb-6">
      <div
        className={`legitify-filepond relative rounded-3xl border-2 border-dashed cursor-pointer disabled:pointer-events-none disabled:opacity-60 transition-all duration-300 ease-out
          ${isDragging
            ? "border-primary/70 bg-primary/20 shadow-[0_0_0_6px_rgba(59,130,246,0.4)]"
            : "border-primary/30 bg-card/60 hover:border-primary/60 hover:bg-primary/5 dark:hover:bg-primary/20 hover:shadow-[0_0_0_4px_rgba(37,99,235,0.12)] dark:hover:shadow-[0_0_0_6px_rgba(59,130,246,0.35)]"
          }
          ${isAnalyzing ? "pointer-events-none opacity-60" : ""
          }
          `}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
      >
        <input
          ref={inputRef}
          type="file"
          accept=".pdf,.docx,.txt"
          onChange={handleFileSelect}
          className="hidden"
        />

        <div className="fp-center flex flex-col items-center justify-center gap-3 py-12 px-6 text-center">
          <div className="fp-icon w-14 h-14 rounded-2xl bg-primary/20 flex items-center justify-center mb-1">
            <Upload className="w-7 h-7 text-primary" />
          </div>
          <p className="fp-title text-lg font-semibold text-foreground">
            Drag & drop your files here
          </p>
          <p className="fp-sub text-sm text-muted-foreground">
            or <span className="text-primary font-medium">browse</span> to choose files
          </p>
          <p className="fp-formats text-xs text-muted-foreground mt-2">
            Supported formats: PDF, DOCX, TXT
          </p>
        </div>
      </div>

      {files.length > 0 && (
        <div className="mt-6 space-y-3">
          {files.map((file, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-4 bg-card rounded-xl border border-border shadow-sm animate-fade-up"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                  <FileText className="w-5 h-5 text-primary" />
                </div>
                <div>
                  <p className="text-sm font-medium text-foreground truncate max-w-50">
                    {file.name}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {formatFileSize(file.size)}
                  </p>
                </div>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  removeFile(index);
                }}
                className="p-2 rounded-lg hover:bg-destructive/10 text-muted-foreground hover:text-destructive transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ))}

          {files.length > 0 && (
            <div className="mt-6 space-y-3">

              {!resultPdfUrl ? (
                <Button
                  disabled={isAnalyzing}
                  onClick={(e) => {
                    handleUpload();
                  }}
                  className="w-full mt-4 gradient-primary font-semibold py-3 shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center gap-2"
                >
                  {isAnalyzing && (
                    <span className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />
                  )}
                  {isAnalyzing ? "Analyzing" : "Analyze Document"}
                </Button>
              ) : (
                <Button
                  onClick={handleViewPdf}
                  className="w-full mt-4 bg-primary text-white font-semibold py-3 shadow-md hover:shadow-lg transition-all duration-200"
                >
                  View Report
                </Button>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default PdfUploader;