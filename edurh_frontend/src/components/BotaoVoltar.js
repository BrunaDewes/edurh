import React from "react";
import { useNavigate } from "react-router-dom";

export default function BotaoVoltar({ destino = -1 }) {
  const navigate = useNavigate();

  return (
    <button
      onClick={() => navigate(destino)}
      style={{
        backgroundColor: "#93c5fd",
        color: "#1e3a8a",
        border: "none",
        borderRadius: "8px",
        padding: "8px 18px",
        cursor: "pointer",
        fontWeight: "600",
        fontSize: "15px",
        margin: "10px 0",
        alignSelf: "flex-start",
        display: "block",
      }}
    >
      ← Voltar
    </button>
  );
}
