import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Dog } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { mockPacientes, mockPropietarios } from '@/lib/mockData';

export default function Pacientes() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');

  const filteredPacientes = mockPacientes.filter(p =>
    p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.especie.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.raza?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getPropietario = (id: string) => mockPropietarios.find(p => p.id === id);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Pacientes</h1>
          <p className="text-muted-foreground mt-1">Gestión de pacientes de la clínica</p>
        </div>
        <Button onClick={() => navigate('/pacientes/nuevo')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Paciente
        </Button>
      </div>

      <div className="flex gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por nombre, especie o raza..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredPacientes.map((paciente) => {
          const propietario = getPropietario(paciente.propietarioId);
          return (
            <Card
              key={paciente.id}
              className="cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => navigate(`/pacientes/${paciente.id}`)}
            >
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                      <Dog className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <CardTitle className="text-lg">{paciente.nombre}</CardTitle>
                      <p className="text-sm text-muted-foreground">{paciente.raza || 'Sin raza'}</p>
                    </div>
                  </div>
                  <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                    {paciente.especie}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Sexo:</span>
                  <span className="font-medium">{paciente.sexo === 'M' ? 'Macho' : 'Hembra'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Edad:</span>
                  <span className="font-medium">{paciente.edadMeses ? `${Math.floor(paciente.edadMeses / 12)}a ${paciente.edadMeses % 12}m` : 'N/A'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Peso:</span>
                  <span className="font-medium">{paciente.pesoKg ? `${paciente.pesoKg} kg` : 'N/A'}</span>
                </div>
                <div className="pt-2 border-t">
                  <p className="text-xs text-muted-foreground">Propietario</p>
                  <p className="text-sm font-medium">{propietario?.nombre}</p>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {filteredPacientes.length === 0 && (
        <div className="text-center py-12">
          <Dog className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium text-foreground">No se encontraron pacientes</h3>
          <p className="text-muted-foreground mt-1">
            {searchTerm ? 'Intenta con otros términos de búsqueda' : 'Comienza agregando un nuevo paciente'}
          </p>
        </div>
      )}
    </div>
  );
}
