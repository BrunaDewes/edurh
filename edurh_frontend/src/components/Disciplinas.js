import React, { useEffect, useState } from "react";
import BotaoVoltar from "./BotaoVoltar"; 

function Disciplinas() {
  const [disciplinas, setDisciplinas] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/disciplinas")
      .then((res) => res.json())
      .then((data) => setDisciplinas(data))
      .catch((err) => console.error("Erro ao carregar disciplinas:", err));
  }, []);

  return (
    <div style={{ padding: "30px" }}>
      {/*Botão de voltar para a página inicial */}
      <BotaoVoltar destino="/home" />

      <h2 style={{ color: "#1d4ed8", marginBottom: "10px" }}>Disciplinas 📘</h2>
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
            <th>Carga Horária</th>
            <th>Turma</th>
          </tr>
        </thead>
        <tbody>
          {disciplinas.map((d) => (
            <tr key={d.id}>
              <td style={{ padding: "8px", textAlign: "center" }}>{d.id}</td>
              <td>{d.nome}</td>
              <td>{d.cargaHoraria}</td>
              <td>{d.turma ? d.turma.nome : "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Disciplinas;
