import React, { useEffect, useState } from "react";

function Turmas() {
  const [turmas, setTurmas] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/turmas")
      .then((res) => res.json())
      .then((data) => setTurmas(data))
      .catch((err) => console.error("Erro ao carregar turmas:", err));
  }, []);

  return (
    <div style={{ padding: "30px" }}>
      <h2 style={{ color: "#1d4ed8", marginBottom: "10px" }}>Turmas 🏫</h2>
      <table
        style={{
          width: "100%",
          borderCollapse: "collapse",
          background: "#fff",
          borderRadius: "8px",
          boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
        }}
      >
        <thead style={{ backgroundColor: "#1d4ed8", color: "white" }}>
          <tr>
            <th style={{ padding: "10px" }}>ID</th>
            <th>Nome</th>
            <th>Ano</th>
            <th>Matriz</th>
          </tr>
        </thead>
        <tbody>
          {turmas.map((t) => (
            <tr key={t.id}>
              <td style={{ padding: "8px", textAlign: "center" }}>{t.id}</td>
              <td>{t.nome}</td>
              <td>{t.ano}</td>
              <td>{t.matriz ? t.matriz.tipo : "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Turmas;
