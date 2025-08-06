import React from "react";
import { exportToExcel } from "../services/api";

const ExportButton = ({ extractedData }) => {
  const handleExport = async () => {
    try {
      await exportToExcel(extractedData);
    } catch (error) {
      console.error("Error exporting Excel:", error);
    }
  };

  return (
    <div className="flex justify-end">
      <button
        onClick={handleExport}
        className="mt-4 w-[20%] items-center cursor-pointer text-green-900 border-1 rounded-sm font-semibold py-2 hover:bg-green-500 hover:text-white transition duration-300"
      >
        Export to Excel
      </button>
    </div>
  );
};

export default ExportButton;
