import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../config";
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import "../styles/Home.css";

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

export default function Home() {
  const navigate = useNavigate();
  const [usuario, setUsuario] = useState(null);

  const [stats, setStats] = useState({
    professores: 0,
    matrizes: 0,
    turmas: 0,
    disciplinas: 0,
  });

  const [porTurno, setPorTurno] = useState({});
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    carregarUsuario();
    carregarEstatisticas();
  }, []);

  const carregarUsuario = async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${API_BASE_URL}/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.ok) {
        const data = await res.json();
        setUsuario(data);
      } else {
        setMensagem("Erro ao carregar usuário.");
      }
    } catch {
      setMensagem("Falha ao conectar com o servidor.");
    }
  };

  const carregarEstatisticas = async () => {
    try {
      const token = localStorage.getItem("token");

      //1) busca resumo simples no /dashboard/estatisticas
      //2) busca lista de professores pra montar o gráfico por turno
      const [statsRes, profRes] = await Promise.all([
        fetch(`${API_BASE_URL}/dashboard/estatisticas`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        fetch(`${API_BASE_URL}/professores`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      if (!statsRes.ok) {
        const text = await statsRes.text();
        throw new Error(text || `HTTP ${statsRes.status}`);
      }

      const statsData = await statsRes.json();

      setStats({
        professores: statsData.professores || 0,
        matrizes: statsData.matrizes || 0,
        turmas: statsData.turmas || 0,
        disciplinas: statsData.disciplinas || 0, // se o backend ainda não mandar, só fica 0
      });

      const profs = await profRes.json();

      //Conta professores por turno com base na lista de professores
      const contagemTurno = { MANHA: 0, TARDE: 0, NOTURNO: 0, INTEGRAL: 0 };
      profs.forEach((p) => {
        if (p.turno && contagemTurno[p.turno] !== undefined) {
          contagemTurno[p.turno]++;
        }
      });

      setPorTurno(contagemTurno);
    } catch (error) {
      console.error("Erro detalhado:", error);
      setMensagem("Erro ao carregar estatísticas: " + error.message);
    }
  };

  const handleLogout = () => {
    if (window.confirm("Tem certeza que deseja sair?")) {
      localStorage.clear();
      navigate("/login");
    }
  };

  // Dados do gráfico
  const data = {
    labels: ["Manhã", "Tarde", "Noite", "Integral"],
    datasets: [
      {
        label: "Professores por Turno",
        data: [
          porTurno.MANHA || 0,
          porTurno.TARDE || 0,
          porTurno.NOTURNO || 0,
          porTurno.INTEGRAL || 0,
        ],
        backgroundColor: ["#3b82f6", "#f59e0b", "#8b5cf6", "#10b981"],
        borderRadius: 8,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, ticks: { stepSize: 1 } },
    },
  };

  return (
    <div className="home-container">
      <header className="navbar">
        <h1>EduRH - Painel Principal</h1>
        <button className="logout-btn" onClick={handleLogout}>
          Sair
        </button>
      </header>

      <div className="main-layout">
        <aside className="sidebar">
          <ul>
            <li onClick={() => navigate("/professores")}>👩‍🏫 Professores</li>
            <li onClick={() => navigate("/matrizes")}>📊 Matrizes</li>
            <li onClick={() => navigate("/turmas")}>🏫 Turmas</li>
            <li onClick={() => navigate("/disciplinas")}>📝 Disciplinas</li>
            <li onClick={() => navigate("/configuracoes")}>⚙️ Configurações</li>
          </ul>
        </aside>

        <main className="content">
          <h2>
            Bem-vindo(a), {usuario ? usuario.nome : "Carregando..."} 👋
          </h2>

          <p className="subtitle">
            Aqui você poderá gerenciar professores, matrizes, disciplinas e muito mais.
          </p>

          {mensagem && <p className="mensagem-erro">{mensagem}</p>}

          <div className="stats-grid">
            <div className="card">
              <span className="icon">👩‍🏫</span>
              <h3>{stats.professores}</h3>
              <p>Professores</p>
            </div>
            <div className="card">
              <span className="icon">📘</span>
              <h3>{stats.matrizes}</h3>
              <p>Matrizes</p>
            </div>
            <div className="card">
              <span className="icon">🏫</span>
              <h3>{stats.turmas}</h3>
              <p>Turmas</p>
            </div>
            <div className="card">
              <span className="icon">📝</span>
              <h3>{stats.disciplinas}</h3>
              <p>Disciplinas</p>
            </div>
          </div>

          <div className="chart-container">
            <h3>📊 Distribuição de Professores por Turno</h3>
            <Bar data={data} options={options} />
          </div>
        </main>
      </div>
    </div>
  );
}
