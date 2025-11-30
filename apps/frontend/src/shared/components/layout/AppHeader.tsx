import { LogOut, User, Search } from 'lucide-react';
import { useState, useEffect } from 'react';
import { useAuth } from '@core/auth/AuthContext';
import { useNavigate } from 'react-router-dom';
import { SidebarTrigger } from '@shared/components/ui/sidebar';
import { Button } from '@shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@shared/components/ui/dropdown-menu';
import { NotificacionesDropdown } from '@features/notificaciones/components/NotificacionesDropdown';
import { BusquedaGlobal } from '@features/busqueda/components/BusquedaGlobal';

const roleLabels = {
  ADMIN: 'Administrador',
  VET: 'Veterinario',
  RECEPCION: 'Recepcionista',
  ESTUDIANTE: 'Estudiante',
};

export function AppHeader() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [busquedaOpen, setBusquedaOpen] = useState(false);

  // Atajo de teclado Ctrl+K o Cmd+K para abrir búsqueda
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setBusquedaOpen(true);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <header className="h-16 border-b border-border bg-card flex items-center justify-between px-6 sticky top-0 z-10">
      <div className="flex items-center gap-4">
        <SidebarTrigger />
      </div>

      <div className="flex items-center gap-4">
        <Button
          variant="outline"
          className="relative hidden md:flex items-center gap-2 text-muted-foreground"
          onClick={() => setBusquedaOpen(true)}
        >
          <Search className="h-4 w-4" />
          <span className="text-sm">Buscar...</span>
          <kbd className="pointer-events-none absolute right-2 hidden h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium opacity-100 sm:flex">
            <span className="text-xs">⌘</span>K
          </kbd>
        </Button>
        <Button
          variant="ghost"
          size="icon"
          className="md:hidden"
          onClick={() => setBusquedaOpen(true)}
          aria-label="Buscar"
        >
          <Search className="h-4 w-4" />
        </Button>
        <BusquedaGlobal open={busquedaOpen} onOpenChange={setBusquedaOpen} />
        <NotificacionesDropdown />

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="flex items-center gap-2">
              <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center">
                <User className="h-4 w-4 text-primary" />
              </div>
              <div className="text-left hidden md:block">
                <div className="text-sm font-medium">{user?.nombre}</div>
                <div className="text-xs text-muted-foreground">
                  {user?.rol && roleLabels[user.rol]}
                </div>
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56 bg-popover">
            <DropdownMenuLabel>Mi Cuenta</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem 
              className="cursor-pointer"
              onClick={() => navigate('/perfil')}
            >
              <User className="mr-2 h-4 w-4" />
              Perfil
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={logout} className="cursor-pointer text-destructive">
              <LogOut className="mr-2 h-4 w-4" />
              Cerrar Sesión
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
