import React, { useState } from "react";
import FileUpload from "../components/FileUpload";
import ModelSelector from "../components/ModelSelector";
import OptionsSelector from "../components/OptionsSelector";
import ExtractedDataTable from "../components/ExtractedDataTable";
import ExportButton from "../components/ExportButton";
import { extractEntities } from "../services/api";

const Home = () => {
  const [selectedModel, setSelectedModel] = useState("GPT");
  const [selectedOptions, setSelectedOptions] = useState([]);
  const [extractedData, setExtractedData] = useState({});
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleExtract = async () => {
    if (!file) {
      alert("Please upload a file first!");
      return;
    }

    setLoading(true);
    const data = await extractEntities(file, selectedModel, selectedOptions);
    console.log(data);
    setExtractedData(data || {});
    setLoading(false);
    // setFile(null);
    // setSelectedOptions([]);
    // setSelectedModel("GPT");
  };

  return (
    <div className="min-h-screen flex flex-col items-center bg-gray-100 p-4">
      <h1 className="text-3xl font-bold mb-2 text-gray-900">
        AI User Story Extractor
      </h1>
      <div className="w-full max-w-6xl bg-white rounded-lg shadow-lg p-8">
        <div className="flex flex-row space-x-8">
          <FileUpload onFileSelect={setFile} file={file} />
          <OptionsSelector
            selectedOptions={selectedOptions}
            onToggleOption={(option) =>
              setSelectedOptions((prev) =>
                prev.includes(option)
                  ? prev.filter((o) => o !== option)
                  : [...prev, option]
              )
            }
          />
          <ModelSelector
            selectedModel={selectedModel}
            onSelectModel={setSelectedModel}
          />

          <button
            disabled={loading}
            onClick={handleExtract}
            className="my-7 w-[20%] h-12 cursor-pointer bg-slate-500 text-white font-semibold py-2 rounded-lg hover:bg-slate-600 transition duration-200"
          >
            {loading ? "Extracting..." : "Extract"}
          </button>
        </div>
        <ExtractedDataTable extractedData={extractedData} />
        {Object.keys(extractedData).length > 0 && (
          <ExportButton extractedData={extractedData} />
        )}
      </div>
    </div>
  );
};

export default Home;
