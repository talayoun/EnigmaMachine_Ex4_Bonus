import React, { useState } from 'react';
import './App.css';

function App() {
  const [query, setQuery] = useState('');
  const [response, setResponse] = useState({ answer: '', sql: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!query) return;

    setLoading(true);
    try {
      // Points to the Spring Boot endpoint defined in your AIController
      const res = await fetch('http://localhost:8080/enigma/ai', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: query })
      });

      const data = await res.json();
      // Updates state based on AIResponseDTO fields: answer and sql
      setResponse(data);
    } catch (error) {
      console.error("Connection error:", error);
      setResponse({
        answer: "Error: Could not reach the server. Make sure Spring Boot is running.",
        sql: ""
      });
    }
    setLoading(false);
  };

  return (
    <div className="app-container">
      <header>
        <h1>Enigma AI Assistant</h1>
        <p>Ask about machines, processing statistics, or alphabet lengths</p>
      </header>

      <form onSubmit={handleSubmit} className="input-area">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="e.g., How many machines are currently declared?"
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Thinking...' : 'Send'}
        </button>
      </form>

      <div className="results-wrapper">
        {/* Main Window - Verbal AI Response */}
        <div className="window main-answer">
          <h3>AI Response</h3>
          <div className="content">
            {response.answer || "Waiting for your question..."}
          </div>
        </div>

        {/* Secondary Window - Generated SQL Query */}
        <div className="window sql-sidebar">
          <h3>Generated SQL</h3>
          <pre className="sql-box">
            {response.sql || "-- SQL code will appear here"}
          </pre>
        </div>
      </div>
    </div>
  );
}

export default App;