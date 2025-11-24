import React, { useState } from "react";
import { API_BASE_URL } from "../config";
import BotaoVoltar from "./BotaoVoltar";

export default function Relatorios() {
  const [selectedReport, setSelectedReport] = useState(null);

  const [relCh, setRelCh] = useState([]);
  const [relMatrizProf, setRelMatrizProf] = useState([]);
  const [relMatrizDet, setRelMatrizDet] = useState([]);
  const [relDistribuicao, setRelDistribuicao] = useState([]);

  const [erro, setErro] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSelectReport = async (tipo) => {
    setSelectedReport(tipo);
    setErro("");

    // se já foi carregado antes, não busca de novo
    if (
      (tipo === "ch" && relCh.length > 0) ||
      (tipo === "matrizProf" && relMatrizProf.length > 0) ||
      (tipo === "matrizDet" && relMatrizDet.length > 0) ||
      (tipo === "dist" && relDistribuicao.length > 0)
    ) {
      return;
    }

    setLoading(true);
    try {
      const token = localStorage.getItem("token");

      let url = "";
      if (tipo === "ch") url = "/professores/relatorio/ch";
      if (tipo === "matrizProf") url = "/professores/relatorio/matriz";
      if (tipo === "matrizDet")
        url = "/professores/relatorio/matriz-detalhado";
      if (tipo === "dist") url = "/professores/relatorio/distribuicao-ch-turno";

      const res = await fetch(`${API_BASE_URL}${url}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || `HTTP ${res.status}`);
      }

      const data = await res.json();

      if (tipo === "ch") setRelCh(data);
      if (tipo === "matrizProf") setRelMatrizProf(data);
      if (tipo === "matrizDet") setRelMatrizDet(data);
      if (tipo === "dist") setRelDistribuicao(data);
    } catch (e) {
      console.error(e);
      setErro(`Erro ao carregar relatório: ${e.message}`);
    } finally {
      setLoading(false);
    }
  };

  // -------- EXPORTAÇÃO CSV (Excel abre) --------
  const exportarCSV = () => {
    let linhas = [];
    let nomeArquivo = "relatorio";

    if (selectedReport === "ch" && relCh.length > 0) {
      nomeArquivo = "relatorio_carga_horaria";
      linhas.push([
        "Professor",
        "CH (horas RT)",
        "Total de períodos usados",
        "Preparação pra aula (períodos)",
        "Total de CH em períodos",
      ]);
      relCh.forEach((item) => {
        linhas.push([
          item.professor,
          item.cargaHorariaHoras,
          item.totalPeriodos,
          item.preparacaoPeriodos,
          item.periodosParaAulas,
        ]);
      });
    } else if (selectedReport === "matrizProf" && relMatrizProf.length > 0) {
      nomeArquivo = "relatorio_professores_por_matriz";
      linhas.push(["Matriz", "Professores"]);
      relMatrizProf.forEach((item) => {
        linhas.push([
          item.matriz,
          Array.isArray(item.professores)
            ? item.professores.join(", ")
            : "",
        ]);
      });
    } else if (selectedReport === "matrizDet" && relMatrizDet.length > 0) {
      nomeArquivo = "relatorio_matriz_turma_disciplina";
      linhas.push([
        "Matriz",
        "Turno",
        "Turma",
        "Disciplina",
        "Professores",
        "CH (períodos)",
      ]);
      relMatrizDet.forEach((item) => {
        linhas.push([
          item.matrizTipo,
          item.turno,
          item.turma,
          item.disciplina,
          Array.isArray(item.professores)
            ? item.professores.join(", ")
            : "",
          item.cargaHoraria,
        ]);
      });
    } else if (selectedReport === "dist" && relDistribuicao.length > 0) {
      nomeArquivo = "relatorio_distribuicao_ch_turno";
      linhas.push([
        "Professor",
        "Período/Turno",
        "Total de períodos usados",
        "Limite pelo RT (períodos)",
        "Períodos livres (globais)",
        "Disciplinas/Turmas",
      ]);
      relDistribuicao.forEach((item) => {
        linhas.push([
          item.professor,
          item.periodo,
          item.totalPeriodos,
          item.maxPeriodos,
          item.periodosLivresTotal,
          Array.isArray(item.disciplinasTurmas)
            ? item.disciplinasTurmas.join(", ")
            : "",
        ]);
      });
    }

    if (linhas.length === 0) {
      alert("Não há dados para exportar neste relatório.");
      return;
    }

    const csvContent = linhas
      .map((linha) =>
        linha
          .map((campo) => {
            const texto = campo != null ? String(campo) : "";
            // coloca entre aspas e troca aspas internas
            return `"${texto.replace(/"/g, '""')}"`;
          })
          .join(";") // separador ; porque é comum no pt-BR
      )
      .join("\n");

    const blob = new Blob([csvContent], {
      type: "text/csv;charset=utf-8;",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${nomeArquivo}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const renderConteudo = () => {
    if (!selectedReport) {
      return (
        <p style={styles.info}>
          Selecione um relatório ao lado para visualizar.
        </p>
      );
    }

    if (loading) {
      return <p style={styles.info}>Carregando relatório...</p>;
    }

    if (erro) {
      return <p style={styles.erro}>{erro}</p>;
    }

    if (selectedReport === "ch") {
      return (
        <section style={styles.section}>
          <div style={styles.headerSection}>
            <h3 style={styles.subtitulo}>Carga horária por professor</h3>
            {relCh.length > 0 && (
              <button style={styles.btnExport} onClick={exportarCSV}>
                ⬇ Exportar para Excel (CSV)
              </button>
            )}
          </div>
          {relCh.length === 0 ? (
            <p>Nenhum dado encontrado.</p>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>Professor</th>
                  <th style={styles.th}>CH (horas RT)</th>
                  <th style={styles.th}>Total de períodos usados</th>
                  <th style={styles.th}>Preparação de aulas (períodos)</th>
                  <th style={styles.th}>Total de CH em períodos</th>
                </tr>
              </thead>
              <tbody>
                {relCh.map((item, idx) => (
                  <tr key={idx}>
                    <td style={styles.td}>{item.professor}</td>
                    <td style={styles.td}>{item.cargaHorariaHoras}</td>
                    <td style={styles.td}>{item.totalPeriodos}</td>
                    <td style={styles.td}>{item.preparacaoPeriodos}</td>
                    <td style={styles.td}>{item.periodosParaAulas}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      );
    }

    if (selectedReport === "matrizProf") {
      return (
        <section style={styles.section}>
          <div style={styles.headerSection}>
            <h3 style={styles.subtitulo}>Professores por matriz</h3>
            {relMatrizProf.length > 0 && (
              <button style={styles.btnExport} onClick={exportarCSV}>
                ⬇ Exportar para Excel (CSV)
              </button>
            )}
          </div>
          {relMatrizProf.length === 0 ? (
            <p>Nenhum dado encontrado.</p>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>Matriz</th>
                  <th style={styles.th}>Professores</th>
                </tr>
              </thead>
              <tbody>
                {relMatrizProf.map((item, idx) => (
                  <tr key={idx}>
                    <td style={styles.td}>{item.matriz}</td>
                    <td style={styles.td}>
                      {Array.isArray(item.professores)
                        ? item.professores.join(", ")
                        : ""}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      );
    }

    if (selectedReport === "matrizDet") {
      return (
        <section style={styles.section}>
          <div style={styles.headerSection}>
            <h3 style={styles.subtitulo}>
              Detalhamento: Matriz → Turma → Disciplina → Professores
            </h3>
            {relMatrizDet.length > 0 && (
              <button style={styles.btnExport} onClick={exportarCSV}>
                ⬇ Exportar para Excel (CSV)
              </button>
            )}
          </div>
          {relMatrizDet.length === 0 ? (
            <p>Nenhum dado encontrado.</p>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>Matriz</th>
                  <th style={styles.th}>Turno</th>
                  <th style={styles.th}>Turma</th>
                  <th style={styles.th}>Disciplina</th>
                  <th style={styles.th}>Professores</th>
                  <th style={styles.th}>CH (períodos)</th>
                </tr>
              </thead>
              <tbody>
                {relMatrizDet.map((item, idx) => (
                  <tr key={idx}>
                    <td style={styles.td}>{item.matrizTipo}</td>
                    <td style={styles.td}>{item.turno}</td>
                    <td style={styles.td}>{item.turma}</td>
                    <td style={styles.td}>{item.disciplina}</td>
                    <td style={styles.td}>
                      {Array.isArray(item.professores)
                        ? item.professores.join(", ")
                        : ""}
                    </td>
                    <td style={styles.td}>{item.cargaHoraria}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      );
    }

    if (selectedReport === "dist") {
      return (
        <section style={styles.section}>
      
          <div style={styles.headerSection}>
            <h3 style={styles.subtitulo}>
              Distribuição de CH por período (turno) por professor
            </h3>
            {relDistribuicao.length > 0 && (
              <button style={styles.btnExport} onClick={exportarCSV}>
                ⬇ Exportar para Excel (CSV)
              </button>
            )}
          </div>

          {relDistribuicao.length === 0 ? (
            <p>Nenhum dado encontrado.</p>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>Professor</th>
                  <th style={styles.th}>Período / Turno</th>
                  <th style={styles.th}>Total de períodos usados (somando disciplinas nas turmas)</th>
                  <th style={styles.th}>Limite pelo RT (períodos)</th>
                  <th style={styles.th}>Períodos livres (globais)</th>
                  <th style={styles.th}>Disciplinas/Turmas</th>
                </tr>
              </thead>
              <tbody>
                {relDistribuicao.map((item, idx) => (
                  <tr key={idx}>
                    <td style={styles.td}>{item.professor}</td>
                    <td style={styles.td}>{item.periodo}</td>
                    <td style={styles.td}>{item.totalPeriodos}</td>
                    <td style={styles.td}>{item.maxPeriodos}</td>
                    <td style={styles.td}>{item.periodosLivresTotal}</td>   {/* disponibilidade GLOBAL correta */}
                    <td style={styles.td}>
                      {Array.isArray(item.disciplinasTurmas)
                        ? item.disciplinasTurmas.join(", ")
                        : ""}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      );
    }

    return null;
  };

  return (
    <div style={styles.page}>
      <div style={styles.sidebar}>
        <BotaoVoltar destino="/home" />
        <h2 style={styles.titulo}>📊 Relatórios</h2>
        <button
          style={{
            ...styles.btn,
            ...(selectedReport === "ch" ? styles.btnActive : {}),
          }}
          onClick={() => handleSelectReport("ch")}
        >
          Carga horária por professor
        </button>
        <button
          style={{
            ...styles.btn,
            ...(selectedReport === "matrizProf" ? styles.btnActive : {}),
          }}
          onClick={() => handleSelectReport("matrizProf")}
        >
          Professores por matriz
        </button>
        <button
          style={{
            ...styles.btn,
            ...(selectedReport === "matrizDet" ? styles.btnActive : {}),
          }}
          onClick={() => handleSelectReport("matrizDet")}
        >
          Matriz → Turma → Disciplina
        </button>
        <button
          style={{
            ...styles.btn,
            ...(selectedReport === "dist" ? styles.btnActive : {}),
          }}
          onClick={() => handleSelectReport("dist")}
        >
          Distribuição de CH por período
        </button>
      </div>

      <div style={styles.content}>{renderConteudo()}</div>
    </div>
  );
}

const styles = {
  page: {
    display: "flex",
    padding: "20px",
    fontFamily: "Segoe UI",
  },
  sidebar: {
    width: "260px",
    marginRight: "20px",
    background: "#f3f4f6",
    padding: "15px",
    borderRadius: "10px",
    boxShadow: "0 2px 5px rgba(0, 0, 0, 0.05)",
    height: "fit-content",
  },
  titulo: {
    fontSize: "18px",
    marginBottom: "15px",
    color: "#1d4ed8",
  },
  btn: {
    width: "100%",
    padding: "8px 10px",
    marginBottom: "8px",
    background: "#1d4ed8",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    textAlign: "left",
    cursor: "pointer",
    fontSize: "14px",
  },
  btnActive: {
    background: "#2563eb",
    color: "#fff",
    fontWeight: "600",
  },
  content: {
    flex: 1,
    background: "#f9fafb",
    padding: "15px",
    borderRadius: "10px",
    boxShadow: "0 2px 5px rgba(0, 0, 0, 0.05)",
    minHeight: "200px",
  },
  section: {
    marginBottom: "20px",
  },
  headerSection: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "10px",
    gap: "10px",
  },
  btnExport: {
    padding: "6px 10px",
    borderRadius: "6px",
    border: "none",
    background: "#059669",
    color: "#fff",
    cursor: "pointer",
    fontSize: "13px",
    fontWeight: 600,
    // 👇 forçar a não ocupar a largura toda
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    width: "auto",
    maxWidth: "260px",
    whiteSpace: "nowrap",
  },
  subtitulo: {
    marginBottom: 0,
    color: "#111827",
  },
  erro: {
    color: "red",
    marginBottom: "10px",
  },
  info: {
    color: "#4b5563",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
    fontSize: "13px",
  },
  th: {
    background: "#e0e7ff",
    padding: "6px 8px",
    border: "1px solid #ddd",
    textAlign: "left",
  },
  td: {
    border: "1px solid #ddd",
    padding: "6px 8px",
    verticalAlign: "top",
  },
};
