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
import PacienteForm from "./pages/PacienteForm";
import Propietarios from "./pages/Propietarios";
import PropietarioDetalle from "./pages/PropietarioDetalle";
import PropietarioForm from "./pages/PropietarioForm";
import Agenda from "./pages/Agenda";
import CitaForm from "./pages/CitaForm";
import HistoriasClinicas from "./pages/HistoriasClinicas";
import HistoriaDetalle from "./pages/HistoriaDetalle";
import ConsultaForm from "./pages/ConsultaForm";
import Prescripciones from "./pages/Prescripciones";
import PrescripcionDetalle from "./pages/PrescripcionDetalle";
import PrescripcionForm from "./pages/PrescripcionForm";
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
            <Route path="/login" element={<AppRoutes />} />
            
            <Route element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }>
              <Route path="/" element={<Dashboard />} />
              <Route path="/pacientes" element={<Pacientes />} />
              <Route path="/pacientes/nuevo" element={<PacienteForm />} />
              <Route path="/pacientes/:id" element={<PacienteDetalle />} />
              <Route path="/pacientes/:id/editar" element={<PacienteForm />} />
              <Route path="/propietarios" element={<Propietarios />} />
              <Route path="/propietarios/nuevo" element={<PropietarioForm />} />
              <Route path="/propietarios/:id" element={<PropietarioDetalle />} />
              <Route path="/propietarios/:id/editar" element={<PropietarioForm />} />
              <Route path="/agenda" element={<Agenda />} />
              <Route path="/agenda/nuevo" element={<CitaForm />} />
              <Route path="/agenda/:id/editar" element={<CitaForm />} />
              <Route path="/historias" element={<HistoriasClinicas />} />
              <Route path="/historias/:id" element={<HistoriaDetalle />} />
              <Route path="/historias/:pacienteId/nueva-consulta" element={<ConsultaForm />} />
              <Route path="/prescripciones" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                  <Prescripciones />
                </ProtectedRoute>
              } />
              <Route path="/prescripciones/nuevo" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                  <PrescripcionForm />
                </ProtectedRoute>
              } />
              <Route path="/prescripciones/:id" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                  <PrescripcionDetalle />
                </ProtectedRoute>
              } />
              <Route path="/reportes" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                  <Reportes />
                </ProtectedRoute>
              } />
              <Route path="/seguridad/roles" element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <SeguridadRoles />
                </ProtectedRoute>
              } />
              <Route path="/seguridad/usuarios" element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <SeguridadUsuarios />
                </ProtectedRoute>
              } />
            </Route>

            <Route path="*" element={<NotFound />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
