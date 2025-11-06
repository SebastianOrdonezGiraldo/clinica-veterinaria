import {
  LayoutDashboard,
  Users,
  Dog,
  Calendar,
  FileText,
  Pill,
  BarChart3,
  Shield,
  ChevronRight,
} from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Rol } from '@/types';
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar';

interface NavItem {
  title: string;
  url: string;
  icon: any;
  roles?: Rol[];
}

const navItems: NavItem[] = [
  { title: 'Dashboard', url: '/', icon: LayoutDashboard },
  { title: 'Pacientes', url: '/pacientes', icon: Dog, roles: ['ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE'] },
  { title: 'Propietarios', url: '/propietarios', icon: Users, roles: ['ADMIN', 'VET', 'RECEPCION'] },
  { title: 'Agenda', url: '/agenda', icon: Calendar, roles: ['ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE'] },
  { title: 'Historias', url: '/historias', icon: FileText, roles: ['ADMIN', 'VET', 'ESTUDIANTE'] },
  { title: 'Prescripciones', url: '/prescripciones', icon: Pill, roles: ['ADMIN', 'VET'] },
  { title: 'Reportes', url: '/reportes', icon: BarChart3, roles: ['ADMIN', 'VET'] },
  { title: 'Seguridad', url: '/seguridad/roles', icon: Shield, roles: ['ADMIN'] },
];

export function AppSidebar() {
  const { user } = useAuth();
  const { open } = useSidebar();

  const filteredItems = navItems.filter(item => 
    !item.roles || (user?.rol && item.roles.includes(user.rol))
  );

  return (
    <Sidebar collapsible="icon" className="border-r border-sidebar-border">
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="text-sidebar-primary font-semibold text-lg px-4 py-4">
            {open ? '🐾 VetClinic Pro' : '🐾'}
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {filteredItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink
                      to={item.url}
                      end={item.url === '/'}
                      className={({ isActive }) =>
                        isActive
                          ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                          : 'hover:bg-sidebar-accent/50'
                      }
                    >
                      <item.icon className="h-4 w-4" />
                      {open && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}
