import React, { useState, useEffect, useRef } from "react";

const OptionsSelector = ({ selectedOptions, onToggleOption }) => {
  const options = ["Classes", "Relationships"];
  const [isOpen, setIsOpen] = useState(false); // State to manage dropdown visibility
  const dropdownRef = useRef(null); // Ref to manage dropdown click outside

  const toggleDropdown = () => {
    setIsOpen((prev) => !prev);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div className="relative mb-4 w-[50%]" ref={dropdownRef}>
      <label className="block text-gray-700 font-semibold mb-2">
        Select Extraction Options:
      </label>
      <button
        onClick={toggleDropdown}
        className="w-full border-1 p-2 rounded-sm text-left focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
      >
        {selectedOptions.length > 0
          ? `Selected: ${selectedOptions.join(", ")}`
          : "Select an entity"}{" "}
        {/* Updated text when no options are selected */}
      </button>
      {isOpen && (
        <div className="absolute z-10 mt-2 w-full bg-white border border-gray-300 rounded-lg shadow-md p-4">
          {options.map((option) => (
            <div key={option} className="flex items-center mb-2">
              <input
                type="checkbox"
                checked={selectedOptions.includes(option)}
                onChange={() => onToggleOption(option)}
                className="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 focus:ring-2 transition duration-200"
              />
              <label className="ml-2 text-gray-700">{option}</label>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default OptionsSelector;
