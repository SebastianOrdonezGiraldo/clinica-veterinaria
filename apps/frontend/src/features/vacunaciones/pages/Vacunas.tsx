import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Edit, Trash2, Syringe, AlertCircle } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Badge } from '@shared/components/ui/badge';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { useVacunasSearch, useDeleteVacuna } from '../hooks/useVacunas';
import { useDebounce } from '@shared/hooks/useDebounce';
import { Vacuna } from '@core/types';

export default function Vacunas() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [especieFiltro, setEspecieFiltro] = useState<string>('todas');
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const debouncedSearch = useDebounce(searchTerm, 300);

  const { data: vacunasPage, isLoading } = useVacunasSearch({
    page,
    size: 20,
    sort: 'nombre,asc',
    nombre: debouncedSearch || undefined,
    especie: especieFiltro !== 'todas' ? especieFiltro : undefined,
  });

  const { mutate: deleteVacuna, isPending: isDeleting } = useDeleteVacuna();

  const vacunas = vacunasPage?.content || [];

  const handleDelete = () => {
    if (deleteId) {
      deleteVacuna(deleteId);
      setDeleteId(null);
    }
  };

  const especies = ['Canino', 'Felino', 'Ave', 'Roedor', 'Reptil', 'Otro'];

  if (isLoading) {
    return <LoadingCards count={6} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Tipos de Vacunas</h1>
          <p className="text-muted-foreground mt-1">Gestiona los tipos de vacunas disponibles</p>
        </div>
        <Button onClick={() => navigate('/vacunaciones/vacunas/nueva')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nueva Vacuna
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por nombre..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={especieFiltro} onValueChange={setEspecieFiltro}>
          <SelectTrigger>
            <SelectValue placeholder="Filtrar por especie" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="todas">Todas las especies</SelectItem>
            {especies.map((especie) => (
              <SelectItem key={especie} value={especie}>
                {especie}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {vacunas.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Syringe className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground text-center">
              {searchTerm || especieFiltro !== 'todas'
                ? 'No se encontraron vacunas'
                : 'No hay vacunas registradas'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {vacunas.map((vacuna) => (
            <Card
              key={vacuna.id}
              className="hover:shadow-md transition-shadow"
            >
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <CardTitle className="text-lg">{vacuna.nombre}</CardTitle>
                    {vacuna.especie && (
                      <Badge variant="outline" className="mt-2">
                        {vacuna.especie}
                      </Badge>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => navigate(`/vacunaciones/vacunas/${vacuna.id}/editar`)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setDeleteId(vacuna.id)}
                      disabled={isDeleting}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Número de dosis:</span>
                    <span className="font-medium">{vacuna.numeroDosis}</span>
                  </div>
                  {vacuna.intervaloDias && (
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Intervalo:</span>
                      <span className="font-medium">{vacuna.intervaloDias} días</span>
                    </div>
                  )}
                  {vacuna.fabricante && (
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Fabricante:</span>
                      <span className="font-medium">{vacuna.fabricante}</span>
                    </div>
                  )}
                  {vacuna.descripcion && (
                    <p className="text-sm text-muted-foreground mt-2 line-clamp-2">
                      {vacuna.descripcion}
                    </p>
                  )}
                  <div className="flex items-center gap-2 mt-4">
                    {vacuna.activo ? (
                      <Badge className="bg-green-500/10 text-green-600 border-green-500/20">
                        Activa
                      </Badge>
                    ) : (
                      <Badge variant="secondary">Inactiva</Badge>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {vacunasPage && vacunasPage.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            Anterior
          </Button>
          <span className="text-sm text-muted-foreground">
            Página {page + 1} de {vacunasPage.totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setPage(p => Math.min(vacunasPage.totalPages - 1, p + 1))}
            disabled={page >= vacunasPage.totalPages - 1}
          >
            Siguiente
          </Button>
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar vacuna?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción desactivará la vacuna. No podrás usarla para nuevas vacunaciones,
              pero los registros existentes se mantendrán.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground">
              Eliminar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

