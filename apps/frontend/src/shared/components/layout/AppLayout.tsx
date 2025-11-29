import { Outlet } from 'react-router-dom';
import { AppSidebar } from './AppSidebar';
import { AppHeader } from './AppHeader';
import { SidebarProvider } from '@shared/components/ui/sidebar';

/**
 * Layout principal de la aplicación.
 *
 * Estructura base que incluye sidebar lateral, header superior y área de contenido principal.
 * Utiliza React Router Outlet para renderizar las rutas hijas.
 * Diseñado para ocupar todo el viewport con sidebar colapsable.
 *
 * @component
 *
 * @returns {JSX.Element} Layout con sidebar, header y área de contenido
 *
 * @example
 * ```tsx
 * // En la configuración de rutas
 * <Route element={<AppLayout />}>
 *   <Route path="dashboard" element={<Dashboard />} />
 *   <Route path="pacientes" element={<Pacientes />} />
 * </Route>
 * ```
 *
 * @see {@link AppSidebar}
 * @see {@link AppHeader}
 */
export function AppLayout() {
  return (
    <SidebarProvider>
      <div className="min-h-screen flex w-full bg-background">
        <AppSidebar />
        <div className="flex-1 flex flex-col">
          <AppHeader />
          <main className="flex-1 p-6">
            <Outlet />
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}
