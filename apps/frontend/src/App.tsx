import { Toaster } from "@shared/components/ui/toaster";
import { Toaster as Sonner } from "@shared/components/ui/sonner";
import { TooltipProvider } from "@shared/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@core/auth/AuthContext";
import { ClienteAuthProvider } from "@core/auth/ClienteAuthContext";
import { ProtectedRoute } from "@shared/components/common/ProtectedRoute";
import { ClienteProtectedRoute } from "@shared/components/common/ClienteProtectedRoute";
import { AppLayout } from "@shared/components/layout/AppLayout";

// Pages
import Login from "@features/auth/pages/Login";
import LandingPage from "@features/landing/pages/LandingPage";
import Dashboard from "@features/dashboard/pages/Dashboard";
import Pacientes from "@features/pacientes/pages/Pacientes";
import PacienteDetalle from "@features/pacientes/pages/PacienteDetalle";
import PacienteForm from "@features/pacientes/pages/PacienteForm";
import Propietarios from "@features/propietarios/pages/Propietarios";
import PropietarioDetalle from "@features/propietarios/pages/PropietarioDetalle";
import PropietarioForm from "@features/propietarios/pages/PropietarioForm";
import Agenda from "@features/agenda/pages/Agenda";
import CitaForm from "@features/agenda/pages/CitaForm";
import CitaDetalle from "@features/agenda/pages/CitaDetalle";
import AgendarCitaPublica from "@features/agenda/pages/AgendarCitaPublica";
import ClienteLogin from "@features/clientes/pages/ClienteLogin";
import ClienteDashboard from "@features/clientes/pages/ClienteDashboard";
import HistoriasClinicas from "@features/historias/pages/HistoriasClinicas";
import HistoriaDetalle from "@features/historias/pages/HistoriaDetalle";
import ConsultaForm from "@features/historias/pages/ConsultaForm";
import Consultas from "@features/consultas/pages/Consultas";
import ConsultaDesdeCita from "@features/consultas/pages/ConsultaDesdeCita";
import Prescripciones from "@features/prescripciones/pages/Prescripciones";
import PrescripcionDetalle from "@features/prescripciones/pages/PrescripcionDetalle";
import PrescripcionForm from "@features/prescripciones/pages/PrescripcionForm";
import Reportes from "@features/reportes/pages/Reportes";
import SeguridadRoles from "@features/usuarios/pages/SeguridadRoles";
import SeguridadUsuarios from "@features/usuarios/pages/SeguridadUsuarios";
import Perfil from "@features/usuarios/pages/Perfil";
import NotFound from "@shared/components/common/NotFound";

const queryClient = new QueryClient();

function AppRoutes() {
  const { user } = useAuth();

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Login />;
}

function HomeRoute() {
  const { user } = useAuth();

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return <LandingPage />;
}

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <ClienteAuthProvider>
            <Routes>
              <Route path="/" element={<HomeRoute />} />
              <Route path="/login" element={<AppRoutes />} />
              <Route path="/agendar-cita" element={<AgendarCitaPublica />} />
              
              {/* Rutas del portal del cliente */}
              <Route path="/cliente/login" element={<ClienteLogin />} />
              <Route
                path="/cliente/dashboard"
                element={
                  <ClienteProtectedRoute>
                    <ClienteDashboard />
                  </ClienteProtectedRoute>
                }
              />
            
            <Route element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }>
              <Route path="/dashboard" element={<Dashboard />} />
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
              <Route path="/agenda/:citaId/consulta" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                  <ConsultaDesdeCita />
                </ProtectedRoute>
              } />
              <Route path="/agenda/:id" element={<CitaDetalle />} />
              <Route path="/consultas" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                  <Consultas />
                </ProtectedRoute>
              } />
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
              <Route path="/perfil" element={<Perfil />} />
            </Route>

            <Route path="*" element={<NotFound />} />
          </Routes>
          </ClienteAuthProvider>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
