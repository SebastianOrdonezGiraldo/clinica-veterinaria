import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit, Mail, Phone, MapPin, FileText, Dog } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { getPropietarioById, mockPacientes } from '@/lib/mockData';

export default function PropietarioDetalle() {
  const { id } = useParams();
  const navigate = useNavigate();
  const propietario = id ? getPropietarioById(id) : undefined;
  const pacientes = mockPacientes.filter(p => p.propietarioId === id);

  if (!propietario) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold">Propietario no encontrado</h2>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/propietarios')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">{propietario.nombre}</h1>
          <p className="text-muted-foreground mt-1">
            {pacientes.length} {pacientes.length === 1 ? 'mascota' : 'mascotas'} registradas
          </p>
        </div>
        <Button variant="outline" className="gap-2" onClick={() => navigate(`/propietarios/${id}/editar`)}>
          <Edit className="h-4 w-4" />
          Editar
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Información de Contacto</CardTitle>
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
                <div>
                  <p className="text-sm text-muted-foreground">Email</p>
                  <p className="font-medium">{propietario.email}</p>
                </div>
              </div>
            )}

            {propietario.telefono && (
              <div className="flex items-start gap-3">
                <Phone className="h-5 w-5 text-muted-foreground mt-0.5" />
                <div>
                  <p className="text-sm text-muted-foreground">Teléfono</p>
                  <p className="font-medium">{propietario.telefono}</p>
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

            {!propietario.documento && !propietario.email && !propietario.telefono && !propietario.direccion && (
              <p className="text-sm text-muted-foreground">No hay información de contacto adicional</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Mascotas</CardTitle>
              <Button size="sm" onClick={() => navigate('/pacientes/nuevo')}>
                Agregar Mascota
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {pacientes.length > 0 ? (
              <div className="space-y-3">
                {pacientes.map((paciente) => (
                  <div
                    key={paciente.id}
                    className="flex items-center justify-between p-3 rounded-lg border border-border hover:bg-accent/50 cursor-pointer transition-colors"
                    onClick={() => navigate(`/pacientes/${paciente.id}`)}
                  >
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <Dog className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <p className="font-medium">{paciente.nombre}</p>
                        <p className="text-sm text-muted-foreground">{paciente.raza || 'Sin raza'}</p>
                      </div>
                    </div>
                    <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                      {paciente.especie}
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Dog className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
                <p className="text-muted-foreground">No hay mascotas registradas</p>
                <Button 
                  variant="outline" 
                  size="sm" 
                  className="mt-3"
                  onClick={() => navigate('/pacientes/nuevo')}
                >
                  Registrar Primera Mascota
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

