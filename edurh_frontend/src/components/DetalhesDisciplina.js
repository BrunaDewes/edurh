import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { API_BASE_URL } from "../config";
import BotaoVoltar from "./BotaoVoltar";

export default function DetalhesDisciplina() {
  const { id } = useParams();
  const [disciplina, setDisciplina] = useState(null);
  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const carregarDisciplina = async () => {
      try {
        setLoading(true);
        const res = await fetch(`${API_BASE_URL}/disciplinas/${id}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        if (!res.ok) {
          const txt = await res.text();
          throw new Error(txt || "Erro ao carregar disciplina.");
        }

        const data = await res.json();
        setDisciplina(data);
      } catch (e) {
        console.error(e);
        setErro(e.message);
      } finally {
        setLoading(false);
      }
    };

    carregarDisciplina();
  }, [id]);

  if (loading) {
    return (
      <div style={styles.container}>
        <BotaoVoltar destino="/disciplinas" />
        <p>Carregando disciplina...</p>
      </div>
    );
  }

  if (erro) {
    return (
      <div style={styles.container}>
        <BotaoVoltar destino="/disciplinas" />
        <p style={{ color: "red" }}>{erro}</p>
      </div>
    );
  }

  if (!disciplina) {
    return (
      <div style={styles.container}>
        <BotaoVoltar destino="/disciplinas" />
        <p>Disciplina não encontrada.</p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <BotaoVoltar destino="/disciplinas" />

      <div style={styles.card}>
        <h2 style={styles.titulo}>📚 {disciplina.nome}</h2>
        <p>
          <strong>Carga horária:</strong>{" "}
          {disciplina.cargaHoraria != null
            ? `${disciplina.cargaHoraria} horas`
            : "-"}
        </p>

        <div style={styles.section}>
          <h3>👩‍🏫 Professores vinculados</h3>
          {disciplina.professores && disciplina.professores.length > 0 ? (
            <ul>
              {disciplina.professores.map((p) => (
                <li key={p.id}>
                  {p.nome} {p.turno ? `(${p.turno})` : ""}
                </li>
              ))}
            </ul>
          ) : (
            <p>Nenhum professor vinculado a esta disciplina.</p>
          )}
        </div>

        <div style={styles.section}>
          <h3>🏫 Turmas vinculadas</h3>
          {disciplina.turmas && disciplina.turmas.length > 0 ? (
            <ul>
              {disciplina.turmas.map((t) => (
                <li key={t.id}>{t.nome}</li>
              ))}
            </ul>
          ) : (
            <p>Nenhuma turma vinculada a esta disciplina.</p>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    padding: "20px",
  },
  card: {
    maxWidth: "700px",
    margin: "20px auto",
    padding: "20px",
    borderRadius: "12px",
    background: "#ffffff",
    boxShadow: "0 3px 10px rgba(0,0,0,0.08)",
  },
  titulo: {
    marginBottom: "10px",
  },
  section: {
    marginTop: "20px",
  },
};
