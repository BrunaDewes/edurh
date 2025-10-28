import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../config";
import ModalAdicionarMatriz from "./ModalAdicionarMatriz";

export default function DetalhesProfessor() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [professor, setProfessor] = useState(null);
  const [mostrarModal, setMostrarModal] = useState(false);
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    carregarProfessor();
  }, []);

  const carregarProfessor = async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${API_BASE_URL}/professores/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setProfessor(data);
      } else {
        setMensagem("Erro ao carregar professor.");
      }
    } catch (error) {
      setMensagem("Erro ao conectar com o servidor.");
    }
  };

  const calcularCHAtual = () => {
    if (!professor?.matrizes) return 0;
    return professor.matrizes.reduce((soma, m) => soma + (m.cargaHoraria || 0), 0);
  };

  const excluirMatriz = async (matrizId) => {
    const confirmar = window.confirm("Deseja remover esta matriz?");
    if (!confirmar) return;

    try {
      const token = localStorage.getItem("token");
      const res = await fetch(
        `${API_BASE_URL}/professores/${id}/matriz/${matrizId}`,
        { method: "DELETE", headers: { Authorization: `Bearer ${token}` } }
      );
      if (res.ok) {
        setMensagem("Matriz removida com sucesso!");
        carregarProfessor();
      } else {
        setMensagem("Erro ao remover matriz.");
      }
    } catch {
      setMensagem("Erro ao conectar com o servidor.");
    }
  };

  if (!professor) return <p style={styles.loading}>Carregando...</p>;

  return (
    <div style={styles.container}>
      <button style={styles.voltarBtn} onClick={() => navigate("/professores")}>
        ← Voltar
      </button>

      <h2 style={styles.titulo}>📋 Detalhes de {professor.nome}</h2>

      <div style={styles.infoBox}>
        <p><strong>Turno:</strong> {professor.turno}</p>
        <p><strong>CH Máxima:</strong> {professor.cargaHoraria} horas</p>
        <p><strong>CH Atual:</strong> {calcularCHAtual()} horas</p>
      </div>

      <div style={styles.matrizesBox}>
        <h3>Matrizes Atribuídas</h3>

        {professor.matrizes?.length > 0 ? (
          <table style={styles.table}>
            <thead>
              <tr>
                <th>Tipo da Matriz</th>
                <th>Carga Horária</th>
                <th>Ação</th>
              </tr>
            </thead>
            <tbody>
              {professor.matrizes.map((m) => (
                <tr key={m.id}>
                  <td>{m.tipo}</td>
                  <td>{m.cargaHoraria}h</td>
                  <td>
                    <button
                      style={styles.excluirBtn}
                      onClick={() => excluirMatriz(m.id)}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>Nenhuma matriz atribuída ainda.</p>
        )}
      </div>

      <button style={styles.addBtn} onClick={() => setMostrarModal(true)}>
        ➕ Adicionar Matriz
      </button>

      {mostrarModal && (
        <ModalAdicionarMatriz
          professorId={id}
          onClose={() => setMostrarModal(false)}
          onSuccess={carregarProfessor}
        />
      )}

      {mensagem && <p style={styles.msg}>{mensagem}</p>}
    </div>
  );
}

const styles = {
  container: {
    padding: "30px",
    maxWidth: "800px",
    margin: "0 auto",
    fontFamily: "Segoe UI",
  },
  titulo: {
    color: "#1d4ed8",
    fontWeight: "700",
    textAlign: "center",
    marginBottom: "20px",
  },
  voltarBtn: {
    background: "#8fa5cfff",
    border: "none",
    padding: "8px 16px",
    borderRadius: "8px",
    cursor: "pointer",
    marginBottom: "15px",
  },
  infoBox: {
    background: "#f9fafb",
    padding: "15px",
    borderRadius: "10px",
    marginBottom: "25px",
    boxShadow: "0 2px 5px rgba(0,0,0,0.05)",
  },
  matrizesBox: {
    marginBottom: "20px",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "10px",
  },
  th: {
    background: "#e0e7ff",
    padding: "8px",
  },
  td: {
    border: "1px solid #ddd",
    padding: "8px",
    textAlign: "center",
  },
  excluirBtn: {
    background: "#dc2626",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    padding: "6px 10px",
    cursor: "pointer",
  },
  addBtn: {
    display: "block",
    margin: "0 auto",
    background: "#2563eb",
    color: "#fff",
    padding: "10px 18px",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
    fontWeight: "600",
  },
  msg: {
    textAlign: "center",
    color: "#2563eb",
    marginTop: "15px",
  },
  loading: {
    textAlign: "center",
    marginTop: "40px",
  },
};
