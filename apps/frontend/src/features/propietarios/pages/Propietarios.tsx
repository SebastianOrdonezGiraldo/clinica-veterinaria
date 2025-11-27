import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, User, Mail, Phone, MoreVertical, Edit, Trash2, Eye, Inbox } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@shared/components/ui/dropdown-menu';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { Pagination } from '@shared/components/common/Pagination';
import { toast } from 'sonner';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { Propietario, PageResponse } from '@core/types';
import { useLogger } from '@shared/hooks/useLogger';

/**
 * Componente Propietarios - Gestión de propietarios con paginación backend
 * 
 * MEJORAS IMPLEMENTADAS:
 * - ✓ Paginación del lado del servidor (no carga todos los datos)
 * - ✓ Búsqueda multicritero (nombre, documento, teléfono)
 * - ✓ Debounce en búsqueda (500ms) para reducir llamadas API
 * - ✓ Ordenamiento en backend
 * - ✓ Manejo de errores mejorado
 * 
 * PATRONES APLICADOS:
 * - Observer Pattern: useEffect reacciona a cambios de filtros
 * - Debounce Pattern: Evita llamadas excesivas a la API
 */
export default function Propietarios() {
  const logger = useLogger('Propietarios');
  const navigate = useNavigate();
  
  // ESTADO: Paginación backend (PageResponse en lugar de array simple)
  const [propietariosPage, setPropietariosPage] = useState<PageResponse<Propietario> | null>(null);
  
  // ESTADO: Búsqueda y filtros
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [searchType, setSearchType] = useState<'nombre' | 'documento' | 'telefono' | 'email'>('nombre');
  const [orderBy, setOrderBy] = useState<'nombre,asc' | 'nombre,desc' | 'documento,asc'>('nombre,asc');
  
  // ESTADO: UI
  const [isLoading, setIsLoading] = useState(true);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed para backend
  const itemsPerPage = 9;

  // EFECTO: Debounce para búsqueda (500ms)
  // PATRÓN: Debounce - Retrasa la ejecución hasta que el usuario deje de escribir
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      setCurrentPage(0); // Resetear a primera página al buscar
    }, 500); // 500ms de espera

    return () => clearTimeout(timer);
  }, [searchTerm]);

  // EFECTO: Cargar propietarios cuando cambian los filtros o la paginación
  // PATRÓN: Observer - Reacciona a cambios de dependencias
  useEffect(() => {
    loadPropietarios();
  }, [debouncedSearchTerm, searchType, orderBy, currentPage]);

  /**
   * Carga propietarios con paginación backend
   * 
   * VENTAJAS:
   * - Solo carga los datos necesarios (9 por página)
   * - Búsqueda y filtrado en BD (más rápido)
   * - Ordenamiento en BD (optimizado)
   */
  const loadPropietarios = async () => {
    try {
      setIsLoading(true);
      
      // Construir parámetros de búsqueda según el tipo seleccionado
      const searchParams: any = {
        page: currentPage,
        size: itemsPerPage,
        sort: orderBy,
      };
      
      // Agregar el filtro correspondiente según el tipo de búsqueda
      if (debouncedSearchTerm) {
        if (searchType === 'nombre') {
          searchParams.nombre = debouncedSearchTerm;
        } else if (searchType === 'documento') {
          searchParams.documento = debouncedSearchTerm;
        } else if (searchType === 'telefono') {
          searchParams.telefono = debouncedSearchTerm;
        } else if (searchType === 'email') {
          searchParams.email = debouncedSearchTerm;
        }
      }
      
      const result = await propietarioService.searchWithFilters(searchParams);
      setPropietariosPage(result);
    } catch (error: any) {
      logger.error('Error al cargar lista de propietarios', error, {
        action: 'loadPropietarios',
        searchTerm,
        page: currentPage,
      });
      toast.error(error.response?.data?.mensaje || error.response?.data?.message || 'Error al cargar propietarios');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Elimina un propietario (soft delete)
   * 
   * PATRÓN: Optimistic UI Update
   * - Actualiza la UI inmediatamente
   * - Si falla, recarga los datos
   */
  const handleDelete = async () => {
    if (!deleteId) return;

    try {
      setIsDeleting(true);
      setDeleteId(null); // Cerrar modal

      await propietarioService.delete(deleteId);
      toast.success('Propietario eliminado exitosamente');
      
      // Recargar la página actual
      await loadPropietarios();
    } catch (error: any) {
      logger.error('Error al eliminar propietario', error, {
        action: 'deletePropietario',
        propietarioId: id,
      });
      toast.error(error.response?.data?.mensaje || error.response?.data?.message || 'Error al eliminar el propietario');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Propietarios</h1>
          <p className="text-muted-foreground mt-1">Gestión de propietarios y tutores</p>
        </div>
        <Button onClick={() => navigate('/propietarios/nuevo')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Propietario
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder={`Buscar por ${searchType}...`}
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={searchType} onValueChange={(v) => setSearchType(v as 'nombre' | 'documento' | 'telefono' | 'email')}>
          <SelectTrigger>
            <SelectValue placeholder="Buscar por" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="nombre">Buscar por Nombre</SelectItem>
            <SelectItem value="documento">Buscar por Documento</SelectItem>
            <SelectItem value="telefono">Buscar por Teléfono</SelectItem>
            <SelectItem value="email">Buscar por Email</SelectItem>
          </SelectContent>
        </Select>
        <Select value={orderBy} onValueChange={(v) => setOrderBy(v as any)}>
          <SelectTrigger>
            <SelectValue placeholder="Ordenar por" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="nombre,asc">Nombre (A-Z)</SelectItem>
            <SelectItem value="nombre,desc">Nombre (Z-A)</SelectItem>
            <SelectItem value="documento,asc">Documento</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <LoadingCards count={9} />
      ) : propietariosPage && propietariosPage.content.length > 0 ? (
        <>
          <div className="flex justify-between items-center text-sm text-muted-foreground mb-2">
            <span>
              Mostrando {propietariosPage.content.length} de {propietariosPage.totalElements} propietarios
            </span>
            <span>
              Página {propietariosPage.number + 1} de {propietariosPage.totalPages || 1}
            </span>
          </div>
          
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {propietariosPage.content.map((propietario) => (
              <Card key={propietario.id} className="hover:shadow-lg transition-shadow">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between">
                    <div 
                      className="flex items-center gap-3 flex-1 cursor-pointer"
                      onClick={() => navigate(`/propietarios/${propietario.id}`)}
                    >
                      <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                        <User className="h-6 w-6 text-primary" />
                      </div>
                      <div className="flex-1">
                        <CardTitle className="text-lg">{propietario.nombre}</CardTitle>
                        <p className="text-sm text-muted-foreground">
                          {propietario.documento || 'Sin documento'}
                        </p>
                      </div>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => navigate(`/propietarios/${propietario.id}`)}>
                          <Eye className="h-4 w-4 mr-2" />
                          Ver Detalle
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => navigate(`/propietarios/${propietario.id}/editar`)}>
                          <Edit className="h-4 w-4 mr-2" />
                          Editar
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          onClick={() => setDeleteId(propietario.id)}
                          className="text-destructive"
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Eliminar
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardHeader>
                <CardContent className="space-y-2">
                  {propietario.documento && (
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Documento:</span>
                      <span className="font-medium">{propietario.documento}</span>
                    </div>
                  )}
                  {propietario.email && (
                    <div className="flex items-center gap-2 text-sm">
                      <Mail className="h-4 w-4 text-muted-foreground" />
                      <span className="truncate">{propietario.email}</span>
                    </div>
                  )}
                  {propietario.telefono && (
                    <div className="flex items-center gap-2 text-sm">
                      <Phone className="h-4 w-4 text-muted-foreground" />
                      <span>{propietario.telefono}</span>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>

          {propietariosPage.totalPages > 1 && (
            <Pagination
              currentPage={currentPage + 1} // Mostrar 1-indexed al usuario
              totalPages={propietariosPage.totalPages}
              onPageChange={(page) => setCurrentPage(page - 1)} // Convertir a 0-indexed para backend
              itemsPerPage={itemsPerPage}
              totalItems={propietariosPage.totalElements}
            />
          )}
        </>
      ) : (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="rounded-full bg-muted p-4 mb-4">
              {debouncedSearchTerm ? (
                <Search className="h-8 w-8 text-muted-foreground" />
              ) : (
                <Inbox className="h-8 w-8 text-muted-foreground" />
              )}
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">
              {debouncedSearchTerm ? 'No se encontraron resultados' : 'No hay propietarios registrados'}
            </h3>
            <p className="text-sm text-muted-foreground text-center max-w-sm mb-4">
              {debouncedSearchTerm 
                ? `No se encontraron propietarios que coincidan con "${debouncedSearchTerm}". Intenta con otros términos de búsqueda.`
                : 'Comienza agregando tu primer propietario para gestionar la información de los dueños de las mascotas.'}
            </p>
            {!debouncedSearchTerm && (
              <Button onClick={() => navigate('/propietarios/nuevo')} className="gap-2">
                <Plus className="h-4 w-4" />
                Agregar Primer Propietario
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar propietario?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. El propietario será eliminado permanentemente del sistema.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleDelete} 
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={isDeleting}
            >
              {isDeleting ? 'Eliminando...' : 'Eliminar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
