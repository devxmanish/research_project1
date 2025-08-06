import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api"; // Update with your backend URL

export const extractEntities = async (file, selectedModel, selectedOptions) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("model", selectedModel);
  selectedOptions.forEach((option) => formData.append("options", option));

  try {
    const response = await axios.post(API_BASE_URL + "/extract", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error extracting entities:", error);
    return null;
  }
};

// ✅ Exporting Excel (unchanged)
export const exportToExcel = async (extractedData) => {
  try {
    const response = await fetch(`${API_BASE_URL}/download`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(extractedData),
    });

    if (!response.ok) {
      throw new Error("Failed to export file");
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = "ExtractedData.xlsx";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error("Error exporting Excel:", error);
    throw error;
  }
};
