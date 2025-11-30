import { Suspense, lazy } from "react";
import { Toaster } from "@shared/components/ui/toaster";
import { Toaster as Sonner } from "@shared/components/ui/sonner";
import { TooltipProvider } from "@shared/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@core/auth/AuthContext";
import { ProtectedRoute } from "@shared/components/common/ProtectedRoute";
import { ClienteProtectedRoute } from "@shared/components/common/ClienteProtectedRoute";
import { AppLayout } from "@shared/components/layout/AppLayout";
import { PageLoader } from "@shared/components/common/PageLoader";

// Páginas críticas (cargadas inmediatamente - necesarias para login/landing)
import Login from "@features/auth/pages/Login";
import LandingPage from "@features/landing/pages/LandingPage";
import AgendarCitaPublica from "@features/agenda/pages/AgendarCitaPublica";
import ForgotPassword from "@features/auth/pages/ForgotPassword";
import ResetPassword from "@features/auth/pages/ResetPassword";

// Lazy loading de páginas - Mejora el tiempo de carga inicial
// Solo se cargan cuando el usuario navega a ellas
const Dashboard = lazy(() => import("@features/dashboard/pages/Dashboard"));
const Pacientes = lazy(() => import("@features/pacientes/pages/Pacientes"));
const PacienteDetalle = lazy(() => import("@features/pacientes/pages/PacienteDetalle"));
const PacienteForm = lazy(() => import("@features/pacientes/pages/PacienteForm"));
const Propietarios = lazy(() => import("@features/propietarios/pages/Propietarios"));
const PropietarioDetalle = lazy(() => import("@features/propietarios/pages/PropietarioDetalle"));
const PropietarioForm = lazy(() => import("@features/propietarios/pages/PropietarioForm"));
const Agenda = lazy(() => import("@features/agenda/pages/Agenda"));
const CitaForm = lazy(() => import("@features/agenda/pages/CitaForm"));
const CitaDetalle = lazy(() => import("@features/agenda/pages/CitaDetalle"));
const ClienteDashboard = lazy(() => import("@features/clientes/pages/ClienteDashboard"));
const ClienteLogin = lazy(() => import("@features/clientes/pages/ClienteLogin"));
const AgendarCitaCliente = lazy(() => import("@features/clientes/pages/AgendarCitaCliente"));
const EstablecerPassword = lazy(() => import("@features/clientes/pages/EstablecerPassword"));
const HistoriasClinicas = lazy(() => import("@features/historias/pages/HistoriasClinicas"));
const HistoriaDetalle = lazy(() => import("@features/historias/pages/HistoriaDetalle"));
const ConsultaForm = lazy(() => import("@features/historias/pages/ConsultaForm"));
const Consultas = lazy(() => import("@features/consultas/pages/Consultas"));
const ConsultaDesdeCita = lazy(() => import("@features/consultas/pages/ConsultaDesdeCita"));
const Prescripciones = lazy(() => import("@features/prescripciones/pages/Prescripciones"));
const PrescripcionDetalle = lazy(() => import("@features/prescripciones/pages/PrescripcionDetalle"));
const PrescripcionForm = lazy(() => import("@features/prescripciones/pages/PrescripcionForm"));
const Reportes = lazy(() => import("@features/reportes/pages/Reportes"));
const SeguridadRoles = lazy(() => import("@features/usuarios/pages/SeguridadRoles"));
const SeguridadUsuarios = lazy(() => import("@features/usuarios/pages/SeguridadUsuarios"));
const Perfil = lazy(() => import("@features/usuarios/pages/Perfil"));
const Categorias = lazy(() => import("@features/inventario/pages/Categorias"));
const CategoriaForm = lazy(() => import("@features/inventario/pages/CategoriaForm"));
const Proveedores = lazy(() => import("@features/inventario/pages/Proveedores"));
const ProveedorForm = lazy(() => import("@features/inventario/pages/ProveedorForm"));
const Productos = lazy(() => import("@features/inventario/pages/Productos"));
const ProductoForm = lazy(() => import("@features/inventario/pages/ProductoForm"));
const Movimientos = lazy(() => import("@features/inventario/pages/Movimientos"));
const MovimientoForm = lazy(() => import("@features/inventario/pages/MovimientoForm"));
const Vacunas = lazy(() => import("@features/vacunaciones/pages/Vacunas"));
const VacunaForm = lazy(() => import("@features/vacunaciones/pages/VacunaForm"));
const Vacunaciones = lazy(() => import("@features/vacunaciones/pages/Vacunaciones"));
const VacunacionForm = lazy(() => import("@features/vacunaciones/pages/VacunacionForm"));
const NotFound = lazy(() => import("@shared/components/common/NotFound"));

const queryClient = new QueryClient();

function AppRoutes() {
  const { user, cliente, userType } = useAuth();

  if (user && userType === 'SISTEMA') {
    return <Navigate to="/dashboard" replace />;
  }

  if (cliente && userType === 'CLIENTE') {
    return <Navigate to="/cliente/dashboard" replace />;
  }

  return <Login />;
}

function HomeRoute() {
  const { user, cliente, userType } = useAuth();

  if (user && userType === 'SISTEMA') {
    return <Navigate to="/dashboard" replace />;
  }

  if (cliente && userType === 'CLIENTE') {
    return <Navigate to="/cliente/dashboard" replace />;
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
          <Routes>
              <Route path="/" element={<HomeRoute />} />
              <Route path="/login" element={<AppRoutes />} />
              <Route path="/agendar-cita" element={<AgendarCitaPublica />} />
              <Route path="/forgot-password" element={<ForgotPassword userType="usuario" />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              
              {/* Rutas del portal del cliente - Lazy loaded */}
              <Route 
                path="/cliente/login" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ClienteLogin />
                  </Suspense>
                } 
              />
              <Route 
                path="/cliente/forgot-password" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ForgotPassword userType="cliente" />
                  </Suspense>
                } 
              />
              <Route 
                path="/cliente/reset-password" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ResetPassword />
                  </Suspense>
                } 
              />
              <Route 
                path="/cliente/establecer-password" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <EstablecerPassword />
                  </Suspense>
                } 
              />
              <Route
                path="/cliente/dashboard"
                element={
                  <ClienteProtectedRoute>
                    <Suspense fallback={<PageLoader />}>
                      <ClienteDashboard />
                    </Suspense>
                  </ClienteProtectedRoute>
                }
              />
              <Route
                path="/cliente/agendar-cita"
                element={
                  <ClienteProtectedRoute>
                    <Suspense fallback={<PageLoader />}>
                      <AgendarCitaCliente />
                    </Suspense>
                  </ClienteProtectedRoute>
                }
              />
            
            <Route element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }>
              <Route 
                path="/dashboard" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Dashboard />
                  </Suspense>
                } 
              />
              <Route 
                path="/pacientes" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Pacientes />
                  </Suspense>
                } 
              />
              <Route 
                path="/pacientes/nuevo" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <PacienteForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/pacientes/:id" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <PacienteDetalle />
                  </Suspense>
                } 
              />
              <Route 
                path="/pacientes/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <PacienteForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/propietarios" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Propietarios />
                  </Suspense>
                } 
              />
              <Route 
                path="/propietarios/nuevo" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <PropietarioForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/propietarios/:id" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <PropietarioDetalle />
                  </Suspense>
                } 
              />
              <Route 
                path="/propietarios/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <PropietarioForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/agenda" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Agenda />
                  </Suspense>
                } 
              />
              <Route 
                path="/agenda/nuevo" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <CitaForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/agenda/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <CitaForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/agenda/:citaId/consulta" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                    <Suspense fallback={<PageLoader />}>
                      <ConsultaDesdeCita />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/agenda/:id" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <CitaDetalle />
                  </Suspense>
                } 
              />
              <Route 
                path="/consultas" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                    <Suspense fallback={<PageLoader />}>
                      <Consultas />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/historias" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <HistoriasClinicas />
                  </Suspense>
                } 
              />
              <Route 
                path="/historias/:id" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <HistoriaDetalle />
                  </Suspense>
                } 
              />
              <Route 
                path="/historias/:pacienteId/nueva-consulta" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ConsultaForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/prescripciones" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                    <Suspense fallback={<PageLoader />}>
                      <Prescripciones />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/prescripciones/nuevo" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                    <Suspense fallback={<PageLoader />}>
                      <PrescripcionForm />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/prescripciones/:id" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                    <Suspense fallback={<PageLoader />}>
                      <PrescripcionDetalle />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/reportes" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN', 'VET']}>
                    <Suspense fallback={<PageLoader />}>
                      <Reportes />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/seguridad/roles" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <Suspense fallback={<PageLoader />}>
                      <SeguridadRoles />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/seguridad/usuarios" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <Suspense fallback={<PageLoader />}>
                      <SeguridadUsuarios />
                    </Suspense>
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/perfil" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Perfil />
                  </Suspense>
                } 
              />
              {/* Rutas de Vacunaciones */}
              <Route 
                path="/vacunaciones" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Vacunaciones />
                  </Suspense>
                } 
              />
              <Route 
                path="/vacunaciones/nueva" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <VacunacionForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/vacunaciones/vacunas" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Vacunas />
                  </Suspense>
                } 
              />
              <Route 
                path="/vacunaciones/vacunas/nueva" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <VacunaForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/vacunaciones/vacunas/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <VacunaForm />
                  </Suspense>
                } 
              />
              {/* Rutas de Inventario */}
              <Route 
                path="/inventario/categorias" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Categorias />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/categorias/nueva" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <CategoriaForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/categorias/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <CategoriaForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/proveedores" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Proveedores />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/proveedores/nuevo" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ProveedorForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/proveedores/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ProveedorForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/productos" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Productos />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/productos/nuevo" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ProductoForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/productos/:id/editar" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <ProductoForm />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/movimientos" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <Movimientos />
                  </Suspense>
                } 
              />
              <Route 
                path="/inventario/movimientos/:tipo" 
                element={
                  <Suspense fallback={<PageLoader />}>
                    <MovimientoForm />
                  </Suspense>
                } 
              />
            </Route>

            <Route 
              path="*" 
              element={
                <Suspense fallback={<PageLoader />}>
                  <NotFound />
                </Suspense>
              } 
            />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
