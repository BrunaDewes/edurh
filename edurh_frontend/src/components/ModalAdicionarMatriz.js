import React, { useEffect, useState } from "react";
import { API_BASE_URL } from "../config";

export default function ModalAdicionarMatriz({ professorId, onClose, onSuccess }) {
  const [matrizes, setMatrizes] = useState([]);
  const [selecionada, setSelecionada] = useState(null);
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    carregarMatrizes();
  }, []);

  const carregarMatrizes = async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${API_BASE_URL}/matrizes`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setMatrizes(data);
      } else {
        setMensagem("Erro ao buscar matrizes.");
      }
    } catch {
      setMensagem("Erro de conexão com o servidor.");
    }
  };

  const vincularMatriz = async () => {
    if (!selecionada) {
      setMensagem("Selecione uma matriz primeiro.");
      return;
    }
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(
        `${API_BASE_URL}/professores/${professorId}/matrizes/${selecionada}`,
        {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      if (res.ok) {
        onSuccess(); // recarrega dados no painel
        onClose(); // fecha o modal automaticamente
      } else {
        setMensagem("Erro ao adicionar matriz.");
      }
    } catch {
      setMensagem("Erro de conexão.");
    }
  };

  return (
    <div style={styles.overlay}>
      <div style={styles.modal}>
        <h3 style={{ color: "#1d4ed8" }}>Adicionar Matriz ao Professor</h3>

        {matrizes.length > 0 ? (
          <>
            <select
              style={styles.select}
              value={selecionada || ""}
              onChange={(e) => setSelecionada(e.target.value)}
            >
              <option value="">Selecione uma matriz</option>
              {matrizes
              .filter((m) => m && typeof m === "object")
              .map((m) => (
                <option key={m.id} value={m.id}>
                  {m.tipo} — {m.turno} ({m.cargaHoraria} per.)
                </option>
              ))}
            </select>

            {mensagem && <p style={styles.mensagem}>{mensagem}</p>}

            <div style={styles.actions}>
              <button onClick={vincularMatriz} style={styles.confirm}>
                Confirmar
              </button>
              <button onClick={onClose} style={styles.cancel}>
                Cancelar
              </button>
            </div>
          </>
        ) : (
          <div style={{ marginTop: "15px" }}>
            <p style={{ marginBottom: "20px" }}>Ainda não há matrizes cadastradas.</p>
            <button onClick={onClose} style={styles.okBtn}>OK</button>
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  overlay: {
    position: "fixed",
    top: 0,
    left: 0,
    width: "100vw",
    height: "100vh",
    background: "rgba(0,0,0,0.4)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 1000,
  },
  modal: {
    background: "#fff",
    borderRadius: "10px",
    padding: "25px",
    width: "350px",
    textAlign: "center",
    boxShadow: "0 4px 10px rgba(0,0,0,0.2)",
  },
  select: {
    width: "100%",
    padding: "10px",
    borderRadius: "6px",
    border: "1px solid #ccc",
    marginTop: "15px",
  },
  mensagem: {
    fontSize: "14px",
    color: "#1d4ed8",
    marginTop: "10px",
  },
  actions: {
    display: "flex",
    justifyContent: "space-between",
    marginTop: "20px",
  },
  confirm: {
    backgroundColor: "#2563eb",
    color: "white",
    border: "none",
    padding: "8px 16px",
    borderRadius: "8px",
    cursor: "pointer",
  },
  cancel: {
    backgroundColor: "#8fa5cfff",
    border: "none",
    padding: "8px 16px",
    borderRadius: "8px",
    cursor: "pointer",
  },
  okBtn: {
    backgroundColor: "#2563eb",
    color: "white",
    border: "none",
    padding: "8px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },
};

