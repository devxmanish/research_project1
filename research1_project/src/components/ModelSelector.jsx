import React from "react";

const ModelSelector = ({ selectedModel, onSelectModel }) => {
  const models = ["GPT", "Grok", "DeepSeek", "Gemini", "Llama"];

  return (
    <div className="mb-4 w-[20%]">
      <label className="block text-gray-700 font-semibold mb-2">
        Select LLM Model:
      </label>
      <select
        value={selectedModel}
        onChange={(e) => onSelectModel(e.target.value)}
        className="w-full p-2 border border-gray-300 rounded-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
      >
        {models.map((model) => (
          <option key={model} value={model}>
            {model}
          </option>
        ))}
      </select>
    </div>
  );
};

export default ModelSelector;
