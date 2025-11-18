import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { API_BASE_URL } from "../config";
import BotaoVoltar from "./BotaoVoltar";

export default function DetalhesTurma() {
  const { id } = useParams();
  const [turma, setTurma] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    const carregarTurma = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/turmas/${id}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        if (!res.ok) {
          setErro("Erro ao carregar turma.");
          return;
        }
        const data = await res.json();
        setTurma(data);
      } catch (e) {
        console.error(e);
        setErro("Erro de conexão com o servidor.");
      }
    };

    carregarTurma();
  }, [id]);

  if (!turma && !erro) {
    return <p style={{ padding: "30px" }}>Carregando turma...</p>;
  }

  return (
    <div style={styles.container}>
      <BotaoVoltar destino="/turmas" />

      <h2 style={styles.titulo}>📋 Detalhes da Turma</h2>

      {erro && <p style={styles.erro}>{erro}</p>}

      {turma && (
        <>
          <div style={styles.infoBox}>
            <p>
              <strong>Nome:</strong> {turma.nome}
            </p>
            <p>
              <strong>Matriz:</strong>{" "}
              {turma.matriz ? turma.matriz.tipo : "—"}
            </p>
            {turma.matriz && (
              <>
                <p>
                  <strong>Carga Horária da Matriz:</strong>{" "}
                  {turma.matriz.cargaHoraria}h
                </p>
                <p>
                  <strong>Turno:</strong> {turma.matriz.turno}
                </p>
              </>
            )}
          </div>

          <div style={styles.disciplinasBox}>
            <h3>Disciplinas da Turma</h3>
            {turma.disciplinas && turma.disciplinas.length > 0 ? (
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>Disciplina</th>
                    <th style={styles.th}>Carga Horária</th>
                    <th style={styles.th}>Professores</th>
                  </tr>
                </thead>
                <tbody>
                  {turma.disciplinas.map((disc) => (
                    <tr key={disc.id}>
                      <td style={styles.td}>{disc.nome}</td>
                      <td style={styles.td}>{disc.cargaHoraria}h</td>
                      <td style={styles.td}>
                        {Array.isArray(disc.professores) &&
                        disc.professores.length > 0
                          ? disc.professores.map((p) => p.nome).join(", ")
                          : "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>Não há disciplinas vinculadas a esta turma.</p>
            )}
          </div>
        </>
      )}
    </div>
  );
}

const styles = {
  container: {
    padding: "30px",
    fontFamily: "Segoe UI",
    maxWidth: "900px",
    margin: "0 auto",
  },
  titulo: {
    color: "#1d4ed8",
    marginBottom: "15px",
    fontWeight: "600",
  },
  erro: {
    color: "red",
    marginBottom: "10px",
  },
  infoBox: {
    background: "#f9fafb",
    padding: "15px",
    borderRadius: "10px",
    marginBottom: "20px",
    boxShadow: "0 2px 5px rgba(0,0,0,0.05)",
  },
  disciplinasBox: {
    background: "#fff",
    padding: "15px",
    borderRadius: "10px",
    boxShadow: "0 2px 5px rgba(0,0,0,0.05)",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "10px",
  },
  th: {
    background: "#e0e7ff",
    padding: "8px",
    border: "1px solid #ddd",
    textAlign: "left",
  },
  td: {
    border: "1px solid #ddd",
    padding: "8px",
    verticalAlign: "top",
  },
};
