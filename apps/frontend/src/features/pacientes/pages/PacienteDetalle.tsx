import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit, FileText, Calendar, User, Activity, ClipboardList } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@shared/components/ui/tabs';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { consultaService } from '@features/historias/services/consultaService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { Paciente, Consulta, Propietario } from '@core/types';
import { toast } from 'sonner';
import { useLogger } from '@shared/hooks/useLogger';

export default function PacienteDetalle() {
  const logger = useLogger('PacienteDetalle');
  const { id } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('general');
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [consultas, setConsultas] = useState<Consulta[]>([]);
  const [propietario, setPropietario] = useState<Propietario | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  const loadData = async () => {
    if (!id) return;
    
    try {
      setIsLoading(true);
      const [pacienteData, consultasData] = await Promise.all([
        pacienteService.getById(id),
        consultaService.getByPaciente(id),
      ]);
      
      setPaciente(pacienteData);
      setConsultas(consultasData);
      
      // Cargar propietario si no viene en el paciente
      if (pacienteData.propietarioId && !pacienteData.propietario) {
        try {
          const propietarioData = await propietarioService.getById(pacienteData.propietarioId);
          setPropietario(propietarioData);
        } catch (error) {
          logger.warn('Error al cargar propietario del paciente', {
            action: 'loadPropietario',
            pacienteId: id,
            propietarioId: pacienteData.propietarioId,
          });
        }
      } else if (pacienteData.propietario) {
        setPropietario(pacienteData.propietario);
      }
    } catch (error) {
      logger.error('Error al cargar datos del paciente', error, {
        action: 'loadData',
        pacienteId: id,
      });
      toast.error('Error al cargar los datos del paciente');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Cargando información del paciente...</p>
      </div>
    );
  }

  if (!paciente) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold">Paciente no encontrado</h2>
        <Button onClick={() => navigate('/pacientes')} className="mt-4">
          Volver a Pacientes
        </Button>
      </div>
    );
  }

  const propietarioData = propietario || paciente.propietario;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/pacientes')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">{paciente.nombre}</h1>
          <p className="text-muted-foreground mt-1">{paciente.especie} • {paciente.raza}</p>
        </div>
        <Button variant="outline" className="gap-2" onClick={() => navigate(`/pacientes/${id}/editar`)}>
          <Edit className="h-4 w-4" />
          Editar
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full max-w-md grid-cols-3">
          <TabsTrigger value="general" className="gap-2">
            <ClipboardList className="h-4 w-4" />
            General
          </TabsTrigger>
          <TabsTrigger value="propietario" className="gap-2">
            <User className="h-4 w-4" />
            Propietario
          </TabsTrigger>
          <TabsTrigger value="historial" className="gap-2">
            <Activity className="h-4 w-4" />
            Historial
          </TabsTrigger>
        </TabsList>

        <TabsContent value="general" className="space-y-6 mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Información General</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Especie</span>
                <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                  {paciente.especie}
                </Badge>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Raza</span>
                <span className="font-medium">{paciente.raza || 'N/A'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Sexo</span>
                <span className="font-medium">{paciente.sexo === 'M' ? 'Macho' : 'Hembra'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Edad</span>
                <span className="font-medium">
                  {paciente.edadMeses ? `${Math.floor(paciente.edadMeses / 12)} años ${paciente.edadMeses % 12} meses` : 'N/A'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Peso</span>
                <span className="font-medium">{paciente.pesoKg ? `${paciente.pesoKg} kg` : 'N/A'}</span>
              </div>
              {paciente.microchip && (
                <div className="flex justify-between pt-2 border-t">
                  <span className="text-muted-foreground">Microchip</span>
                  <span className="font-medium">{paciente.microchip}</span>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="propietario" className="space-y-6 mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Información del Propietario</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {propietarioData ? (
                <>
                  <div>
                    <p className="text-sm text-muted-foreground">Nombre</p>
                    <p className="font-medium text-lg">{propietarioData.nombre}</p>
                  </div>
                  {propietarioData.documento && (
                    <div>
                      <p className="text-sm text-muted-foreground">Documento</p>
                      <p className="font-medium">{propietarioData.documento}</p>
                    </div>
                  )}
                  {propietarioData.email && (
                    <div>
                      <p className="text-sm text-muted-foreground">Email</p>
                      <p className="font-medium">{propietarioData.email}</p>
                    </div>
                  )}
                  {propietarioData.telefono && (
                    <div>
                      <p className="text-sm text-muted-foreground">Teléfono</p>
                      <p className="font-medium">{propietarioData.telefono}</p>
                    </div>
                  )}
                  <div className="pt-3">
                    <Button onClick={() => navigate(`/propietarios/${paciente.propietarioId}`)}>
                      Ver Perfil Completo
                    </Button>
                  </div>
                </>
              ) : (
                <p className="text-muted-foreground">No se pudo cargar la información del propietario</p>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="historial" className="space-y-6 mt-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Historial de Atenciones</CardTitle>
                <Button onClick={() => navigate(`/historias/${paciente.id}`)} className="gap-2">
                  <FileText className="h-4 w-4" />
                  Ver Historia Completa
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {consultas.length > 0 ? (
                <div className="space-y-3">
                  {consultas.slice(0, 3).map((consulta) => (
                    <div key={consulta.id} className="p-3 rounded-lg border border-border hover:bg-accent/50 cursor-pointer" onClick={() => navigate(`/historias/${paciente.id}`)}>
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <span className="font-medium">
                              {new Date(consulta.fecha).toLocaleDateString('es-ES', {
                                day: '2-digit',
                                month: 'long',
                                year: 'numeric'
                              })}
                            </span>
                          </div>
                          {consulta.profesionalNombre && (
                            <p className="text-sm text-muted-foreground">
                              Atendido por {consulta.profesionalNombre}
                            </p>
                          )}
                          {consulta.diagnostico && (
                            <div className="flex flex-wrap gap-1 mt-2">
                              <Badge variant="outline" className="text-xs">
                                {consulta.diagnostico}
                              </Badge>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
                  <p className="text-muted-foreground">Sin atenciones registradas</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
