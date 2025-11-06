import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Search, Dog } from 'lucide-react';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { mockPacientes, mockPropietarios, getConsultasByPaciente } from '@shared/utils/mockData';

export default function HistoriasClinicas() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');

  const filteredPacientes = mockPacientes.filter(p =>
    p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    mockPropietarios.find(pr => pr.id === p.propietarioId)?.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Historias Clínicas</h1>
        <p className="text-muted-foreground mt-1">Consulta historias clínicas de pacientes</p>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar paciente o propietario..."
          className="pl-10"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredPacientes.map((paciente) => {
          const propietario = mockPropietarios.find(p => p.id === paciente.propietarioId);
          const consultas = getConsultasByPaciente(paciente.id);
          
          return (
            <Card
              key={paciente.id}
              className="cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => navigate(`/historias/${paciente.id}`)}
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
                <div className="flex items-center gap-2 text-sm">
                  <FileText className="h-4 w-4 text-muted-foreground" />
                  <span className="font-medium">{consultas.length} consultas registradas</span>
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
    </div>
  );
}
