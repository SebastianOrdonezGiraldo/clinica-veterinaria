import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@/contexts/AuthContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { AppLayout } from "@/components/layout/AppLayout";

// Pages
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Pacientes from "./pages/Pacientes";
import PacienteDetalle from "./pages/PacienteDetalle";
import Propietarios from "./pages/Propietarios";
import Agenda from "./pages/Agenda";
import HistoriasClinicas from "./pages/HistoriasClinicas";
import HistoriaDetalle from "./pages/HistoriaDetalle";
import Prescripciones from "./pages/Prescripciones";
import PrescripcionDetalle from "./pages/PrescripcionDetalle";
import Reportes from "./pages/Reportes";
import SeguridadRoles from "./pages/SeguridadRoles";
import SeguridadUsuarios from "./pages/SeguridadUsuarios";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

function AppRoutes() {
  const { user } = useAuth();

  if (user) {
    return <Navigate to="/" replace />;
  }

  return <Login />;
}

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            
            <Route element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }>
              <Route path="/" element={<Dashboard />} />
              <Route path="/pacientes" element={<Pacientes />} />
              <Route path="/pacientes/:id" element={<PacienteDetalle />} />
              <Route path="/propietarios" element={<Propietarios />} />
              <Route path="/agenda" element={<Agenda />} />
              <Route path="/historias" element={<HistoriasClinicas />} />
              <Route path="/historias/:id" element={<HistoriaDetalle />} />
              <Route path="/prescripciones" element={<Prescripciones />} />
              <Route path="/prescripciones/:id" element={<PrescripcionDetalle />} />
              <Route path="/reportes" element={<Reportes />} />
              <Route path="/seguridad/roles" element={<SeguridadRoles />} />
              <Route path="/seguridad/usuarios" element={<SeguridadUsuarios />} />
            </Route>

            <Route path="*" element={<NotFound />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
