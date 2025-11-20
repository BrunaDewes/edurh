import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { API_BASE_URL } from "../config";
import BotaoVoltar from "./BotaoVoltar";

export default function DetalhesTurma() {
  const { id } = useParams();
  const [turma, setTurma] = useState(null);
  const [erro, setErro] = useState("");
  const [todasDisciplinas, setTodasDisciplinas] = useState([]);
  const [disciplinaSelecionada, setDisciplinaSelecionada] = useState("");
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    // carregar a turma
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

    // carregar todas as disciplinas para o select
    const carregarDisciplinas = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/disciplinas`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        if (!res.ok) {
          console.error("Erro ao carregar disciplinas.");
          return;
        }
        const data = await res.json();
        setTodasDisciplinas(data);
      } catch (e) {
        console.error(e);
      }
    };

    carregarTurma();
    carregarDisciplinas();
  }, [id]);

  // handler para vincular disciplina à turma
  const vincularDisciplinaATurma = async (e) => {
    e.preventDefault();
    setMensagem("");
    setErro("");

    if (!disciplinaSelecionada) {
      setErro("Selecione uma disciplina.");
      setTimeout(() => setErro(""), 3000);
      return;
    }

    try {
      const res = await fetch(
        `${API_BASE_URL}/turmas/${id}/disciplinas/${disciplinaSelecionada}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      const txt = await res.text();

      if (!res.ok) {
        setErro(txt || "Erro ao vincular disciplina à turma.");
      } else {
        setMensagem(txt || "Disciplina vinculada à turma!");
        setDisciplinaSelecionada("");

        // recarrega a turma para atualizar a lista de disciplinas
        try {
          const resTurma = await fetch(`${API_BASE_URL}/turmas/${id}`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          });
          if (resTurma.ok) {
            const dataTurma = await resTurma.json();
            setTurma(dataTurma);
          }
        } catch (e) {
          console.error(e);
        }
      }

      setTimeout(() => {
        setMensagem("");
        setErro("");
      }, 3000);
    } catch (e) {
      console.error(e);
      setErro("Erro de conexão com o servidor.");
      setTimeout(() => setErro(""), 3000);
    }
  };

  const removerDisciplina = async (disciplinaId) => {
    const confirmar = window.confirm(
      "Deseja remover esta disciplina da turma?"
    );
    if (!confirmar) return;

    try {
      const token = localStorage.getItem("token");

      // chama o endpoint do backend
      const res = await fetch(
        `${API_BASE_URL}/turmas/${id}/disciplinas/${disciplinaId}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const txt = await res.text();

      if (res.ok) {
        // recarrega a turma para atualizar a lista de disciplinas
        const resTurma = await fetch(`${API_BASE_URL}/turmas/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (resTurma.ok) {
          const data = await resTurma.json();
          setTurma(data);
          setErro("");
        } else {
          setErro("Disciplina removida, mas houve erro ao recarregar a turma.");
        }
      } else {
        setErro(txt || "Erro ao remover disciplina da turma.");
      }
    } catch (e) {
      console.error(e);
      setErro("Erro de conexão ao remover disciplina.");
    }
  };

  if (!turma && !erro) {
    return <p style={{ padding: "30px" }}>Carregando turma...</p>;
  }

  // calcular disciplinas ainda não vinculadas à turma
  const idsDisciplinasTurma = new Set(
    (turma?.disciplinas || []).map((d) => d.id)
  );
  const disciplinasDisponiveis = todasDisciplinas.filter(
    (d) => !idsDisciplinasTurma.has(d.id)
  );

  const totalPeriodos = turma.disciplinas?.reduce(
    (soma, d) => soma + (d.cargaHoraria || 0),
    0
  ) || 0;

  return (
    <div style={styles.container}>
      <BotaoVoltar destino="/turmas" />

      <h2 style={styles.titulo}>📋 Detalhes da Turma</h2>

      {erro && <p style={styles.erro}>{erro}</p>}
      {mensagem && <p style={styles.msg}>{mensagem}</p>}

      {turma && (
        <>
          <div style={styles.infoBox}>
            <p>
              <strong>Nome:</strong> {turma.nome}
            </p>
            <p>
              <strong>Matriz:</strong> {turma.matriz ? turma.matriz.tipo : "—"}
            </p>
            {turma.matriz && (
              <>
                <p>
                  <strong>Carga Horária da Matriz:</strong>{" "}
                  {turma.matriz.cargaHoraria} períodos
                </p>
                <p>
                  <strong>Turno:</strong> {turma.matriz.turno}
                </p>
                <p>
                  <strong>Total de períodos da turma:</strong>{" "}
                  {totalPeriodos} /{" "}
                  {turma.matriz?.cargaHoraria} (períodos)
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
                    <th style={styles.th}>Ação</th>
                  </tr>
                </thead>
                <tbody>
                  {turma.disciplinas
                    .filter((disc) => disc && typeof disc === "object")
                    .map((disc) => (
                      <tr key={disc.id}>
                        <td style={styles.td}>{disc.nome}</td>
                        <td style={styles.td}>{disc.cargaHoraria} períodos</td>
                        <td style={styles.td}>
                          {Array.isArray(disc.professores) &&
                          disc.professores.filter(
                            (p) => p && typeof p === "object"
                          ).length > 0
                            ? disc.professores
                                .filter((p) => p && typeof p === "object")
                                .map((p) => p.nome)
                                .join(", ")
                            : "—"}
                        </td>
                        <td style={styles.td}>
                          <button
                            style={styles.excluirBtn}
                            onClick={() => removerDisciplina(disc.id)}
                          >
                            Remover
                          </button>
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            ) : (
              <p>Não há disciplinas vinculadas a esta turma.</p>
            )}

            {/* formulário para adicionar disciplina à turma */}
            <form
              onSubmit={vincularDisciplinaATurma}
              style={{ marginTop: "15px", display: "flex", gap: "10px" }}
            >
              <select
                value={disciplinaSelecionada}
                onChange={(e) => setDisciplinaSelecionada(e.target.value)}
                style={{ flex: 1, padding: "6px 8px", borderRadius: "8px" }}
              >
                <option value="">Selecione uma disciplina</option>
                {disciplinasDisponiveis.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.nome}{" "}
                    {d.cargaHoraria != null ? `(${d.cargaHoraria} per)` : ""}
                  </option>
                ))}
              </select>

              <button
                type="submit"
                style={{
                  padding: "8px 12px",
                  borderRadius: "8px",
                  border: "none",
                  background: "#2563eb",
                  color: "#fff",
                  cursor: "pointer",
                  fontWeight: "600",
                }}
              >
                ➕ Adicionar disciplina
              </button>
            </form>
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
  msg: {
    color: "#2563eb",
    marginBottom: "10px",
    textAlign: "center",
  },
  infoBox: {
    background: "#f9fafb",
    padding: "15px",
    borderRadius: "10px",
    marginBottom: "20px",
    boxShadow: "0 2px 5px rgba(0, 0, 0, 0.05)",
  },
  disciplinasBox: {
    background: "#fff",
    padding: "15px",
    borderRadius: "10px",
    boxShadow: "0 2px 5px rgba(0, 0, 0, 0.05)",
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
  excluirBtn: {
    background: "#dc2626",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    padding: "6px 10px",
    cursor: "pointer",
  },
};
