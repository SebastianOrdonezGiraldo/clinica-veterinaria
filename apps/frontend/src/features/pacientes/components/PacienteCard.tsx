import { memo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Dog, MoreVertical, Edit, Trash2, Eye } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Button } from '@shared/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@shared/components/ui/dropdown-menu';
import { Paciente, Propietario } from '@core/types';

interface PacienteCardProps {
  paciente: Paciente;
  propietario?: Propietario;
  onDelete: (id: string) => void;
}

/**
 * Componente memoizado para la tarjeta de paciente
 * 
 * Evita re-renders innecesarios cuando la lista de pacientes cambia
 * pero este paciente específico no ha cambiado.
 * 
 * @param paciente - Datos del paciente
 * @param propietario - Datos del propietario (opcional)
 * @param onDelete - Callback para eliminar paciente
 */
export const PacienteCard = memo<PacienteCardProps>(({ paciente, propietario, onDelete }) => {
  const navigate = useNavigate();

  return (
    <Card 
      key={paciente.id} 
      className="hover:shadow-lg transition-shadow"
      role="article"
      aria-label={`Paciente ${paciente.nombre}`}
    >
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div 
            className="flex items-center gap-3 flex-1 cursor-pointer"
            onClick={() => navigate(`/pacientes/${paciente.id}`)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                navigate(`/pacientes/${paciente.id}`);
              }
            }}
            role="button"
            tabIndex={0}
            aria-label={`Ver detalles de ${paciente.nombre}`}
          >
            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center" aria-hidden="true">
              <Dog className="h-6 w-6 text-primary" />
            </div>
            <div>
              <CardTitle className="text-lg">{paciente.nombre}</CardTitle>
              <p className="text-sm text-muted-foreground">{paciente.raza || 'Sin raza'}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
              {paciente.especie}
            </Badge>
            <DropdownMenu>
              <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className="h-8 w-8"
                  aria-label={`Opciones para ${paciente.nombre}`}
                >
                  <MoreVertical className="h-4 w-4" aria-hidden="true" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => navigate(`/pacientes/${paciente.id}`)}>
                  <Eye className="h-4 w-4 mr-2" aria-hidden="true" />
                  Ver Detalle
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate(`/pacientes/${paciente.id}/editar`)}>
                  <Edit className="h-4 w-4 mr-2" aria-hidden="true" />
                  Editar
                </DropdownMenuItem>
                <DropdownMenuItem 
                  onClick={() => onDelete(paciente.id)}
                  className="text-destructive"
                >
                  <Trash2 className="h-4 w-4 mr-2" aria-hidden="true" />
                  Eliminar
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-2">
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">Sexo:</span>
          <span className="font-medium">{paciente.sexo === 'M' ? 'Macho' : 'Hembra'}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">Edad:</span>
          <span className="font-medium">
            {paciente.edadMeses ? `${Math.floor(paciente.edadMeses / 12)}a ${paciente.edadMeses % 12}m` : 'N/A'}
          </span>
        </div>
        {paciente.pesoKg && (
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Peso:</span>
            <span className="font-medium">{paciente.pesoKg} kg</span>
          </div>
        )}
        {propietario && (
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Propietario:</span>
            <span className="font-medium truncate ml-2">{propietario.nombre}</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}, (prevProps, nextProps) => {
  // Comparación personalizada para evitar re-renders innecesarios
  return (
    prevProps.paciente.id === nextProps.paciente.id &&
    prevProps.paciente.nombre === nextProps.paciente.nombre &&
    prevProps.paciente.especie === nextProps.paciente.especie &&
    prevProps.paciente.raza === nextProps.paciente.raza &&
    prevProps.paciente.sexo === nextProps.paciente.sexo &&
    prevProps.paciente.edadMeses === nextProps.paciente.edadMeses &&
    prevProps.paciente.pesoKg === nextProps.paciente.pesoKg &&
    prevProps.propietario?.id === nextProps.propietario?.id &&
    prevProps.propietario?.nombre === nextProps.propietario?.nombre
  );
});

PacienteCard.displayName = 'PacienteCard';

