import React, { useState } from "react";

const FileUpload = ({ onFileSelect, file }) => {
  const [fileName, setFileName] = useState("");

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    setFileName(file.name);
    onFileSelect(file);
  };

  return (
    <div className="flex flex-col items-center mb-4">
      <label className="flex flex-col items-center justify-center h-22 !w-60 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer hover:border-blue-500 transition duration-200 bg-white shadow-md">
        <span className="text-[15px] text-gray-500">
          Drag and drop your file here
        </span>
        <span className="text-[15px] text-gray-400">or</span>
        <span className="text-[15px] text-blue-600">Browse file</span>
        <input
          type="file"
          accept=".txt"
          onChange={handleFileUpload}
          className="hidden"
        />
      </label>
      {fileName && file && (
        <p className="mt-2 text-gray-700">
          Uploaded: <span className="font-semibold">{fileName}</span>
        </p>
      )}
    </div>
  );
};

export default FileUpload;
