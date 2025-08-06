import React from "react";

const ExtractedDataTable = ({ extractedData }) => {
  if (!extractedData || !extractedData.extractedEntities) {
    return <p>No data extracted.</p>;
  }

  const { extractedEntities } = extractedData; // Extract actual data

  return (
    <div>
      <h2>Extracted Entities</h2>
      <div
        style={{
          maxHeight: "400px", // Adjust height as needed
          overflowY: "auto", // Enable vertical scrolling
          border: "1px solid #ccc",
          padding: "10px",
          borderRadius: "8px",
          background: "#f9f9f9",
        }}
      >
        {Object.entries(extractedEntities).map(([category, values]) => (
          <div key={category} style={{ marginBottom: "20px" }}>
            <h3>{category}</h3> {/* Key as Table Header */}
            <table
              border="1"
              cellPadding="8"
              style={{ width: "100%", borderCollapse: "collapse" }}
            >
              <thead>
                <tr>
                  <th>#</th>
                  <th>{category}</th>
                </tr>
              </thead>
              <tbody>
                {Array.isArray(values) ? (
                  values.map((item, index) => (
                    <tr key={index}>
                      <td>{index + 1}</td>
                      <td>{item}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="2">No data available</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ExtractedDataTable;
