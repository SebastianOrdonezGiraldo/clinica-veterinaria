import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Edit, Mail, Phone, MapPin, FileText, Dog, User } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@shared/components/ui/tabs';
import { Skeleton } from '@shared/components/ui/skeleton';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { Propietario, Paciente } from '@core/types';
import { toast } from 'sonner';

export default function PropietarioDetalle() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('informacion');
  const [propietario, setPropietario] = useState<Propietario | null>(null);
  const [mascotas, setMascotas] = useState<Paciente[]>([]);
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
      const [propietarioData, mascotasData] = await Promise.all([
        propietarioService.getById(id),
        pacienteService.getByPropietario(id),
      ]);
      
      setPropietario(propietarioData);
      setMascotas(mascotasData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      toast.error('Error al cargar los datos del propietario');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Skeleton className="h-10 w-10 rounded-md" />
            <div className="space-y-2">
              <Skeleton className="h-8 w-64" />
              <Skeleton className="h-4 w-48" />
            </div>
          </div>
          <Skeleton className="h-10 w-24" />
        </div>
        <div className="space-y-4">
          <Skeleton className="h-10 w-64" />
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-48" />
            </CardHeader>
            <CardContent className="space-y-4">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  if (!propietario) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg font-medium">Propietario no encontrado</h3>
        <Button onClick={() => navigate('/propietarios')} className="mt-4">
          Volver a Propietarios
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/propietarios')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-foreground">{propietario.nombre}</h1>
            <p className="text-muted-foreground mt-1">Información del propietario</p>
          </div>
        </div>
        <Button className="gap-2" onClick={() => navigate(`/propietarios/${id}/editar`)}>
          <Edit className="h-4 w-4" />
          Editar
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full max-w-md grid-cols-2">
          <TabsTrigger value="informacion" className="gap-2">
            <User className="h-4 w-4" />
            Información
          </TabsTrigger>
          <TabsTrigger value="mascotas" className="gap-2">
            <Dog className="h-4 w-4" />
            Mascotas ({mascotas.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="informacion" className="space-y-6 mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Información Personal</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {propietario.documento && (
                <div className="flex items-start gap-3">
                  <FileText className="h-5 w-5 text-muted-foreground mt-0.5" />
                  <div>
                    <p className="text-sm text-muted-foreground">Documento</p>
                    <p className="font-medium">{propietario.documento}</p>
                  </div>
                </div>
              )}
              
              {propietario.email && (
                <div className="flex items-start gap-3">
                  <Mail className="h-5 w-5 text-muted-foreground mt-0.5" />
                  <div className="flex-1">
                    <p className="text-sm text-muted-foreground">Email</p>
                    <a
                      href={`mailto:${propietario.email}`}
                      className="font-medium text-primary hover:underline flex items-center gap-2"
                    >
                      {propietario.email}
                    </a>
                  </div>
                </div>
              )}

              {propietario.telefono && (
                <div className="flex items-start gap-3">
                  <Phone className="h-5 w-5 text-muted-foreground mt-0.5" />
                  <div className="flex-1">
                    <p className="text-sm text-muted-foreground">Teléfono</p>
                    <a
                      href={`tel:${propietario.telefono}`}
                      className="font-medium text-primary hover:underline flex items-center gap-2"
                    >
                      {propietario.telefono}
                    </a>
                  </div>
                </div>
              )}

              {propietario.direccion && (
                <div className="flex items-start gap-3">
                  <MapPin className="h-5 w-5 text-muted-foreground mt-0.5" />
                  <div>
                    <p className="text-sm text-muted-foreground">Dirección</p>
                    <p className="font-medium">{propietario.direccion}</p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="mascotas" className="space-y-6 mt-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <Dog className="h-5 w-5 text-primary" />
                  Mascotas Registradas
                </CardTitle>
                <Button size="sm" onClick={() => navigate('/pacientes/nuevo')}>
                  Agregar Mascota
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {mascotas.length > 0 ? (
                <div className="grid gap-4 md:grid-cols-2">
                  {mascotas.map((mascota) => (
                    <div
                      key={mascota.id}
                      className="p-4 rounded-lg border border-border hover:bg-accent/50 transition-colors cursor-pointer"
                      onClick={() => navigate(`/pacientes/${mascota.id}`)}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h4 className="font-semibold text-lg">{mascota.nombre}</h4>
                          <p className="text-sm text-muted-foreground">{mascota.raza || 'Sin raza'}</p>
                        </div>
                        <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                          {mascota.especie}
                        </Badge>
                      </div>
                      <div className="grid grid-cols-2 gap-2 text-sm mt-3">
                        {mascota.sexo && (
                          <div>
                            <span className="text-muted-foreground">Sexo:</span>
                            <span className="ml-1 font-medium">{mascota.sexo === 'M' ? 'Macho' : 'Hembra'}</span>
                          </div>
                        )}
                        {mascota.edadMeses && (
                          <div>
                            <span className="text-muted-foreground">Edad:</span>
                            <span className="ml-1 font-medium">{Math.floor(mascota.edadMeses / 12)} años</span>
                          </div>
                        )}
                        {mascota.pesoKg && (
                          <div>
                            <span className="text-muted-foreground">Peso:</span>
                            <span className="ml-1 font-medium">{mascota.pesoKg} kg</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Dog className="h-12 w-12 text-muted-foreground mx-auto mb-3" />
                  <p className="text-muted-foreground">No hay mascotas registradas</p>
                  <Button variant="outline" className="mt-4" onClick={() => navigate('/pacientes/nuevo')}>
                    Agregar Primera Mascota
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
