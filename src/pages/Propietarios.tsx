import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, User, Mail, Phone } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { mockPropietarios, mockPacientes } from '@/lib/mockData';

export default function Propietarios() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');

  const filteredPropietarios = mockPropietarios.filter(p =>
    p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.documento?.includes(searchTerm)
  );

  const getPacientesCount = (propietarioId: string) => 
    mockPacientes.filter(p => p.propietarioId === propietarioId).length;

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

      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar por nombre, documento o email..."
          className="pl-10"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredPropietarios.map((propietario) => (
          <Card
            key={propietario.id}
            className="cursor-pointer hover:shadow-lg transition-shadow"
            onClick={() => navigate(`/propietarios/${propietario.id}`)}
          >
            <CardHeader className="pb-3">
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <User className="h-6 w-6 text-primary" />
                </div>
                <div className="flex-1">
                  <CardTitle className="text-lg">{propietario.nombre}</CardTitle>
                  <p className="text-sm text-muted-foreground">
                    {getPacientesCount(propietario.id)} {getPacientesCount(propietario.id) === 1 ? 'mascota' : 'mascotas'}
                  </p>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-2">
              {propietario.documento && (
                <div className="flex items-center gap-2 text-sm">
                  <span className="text-muted-foreground">Doc:</span>
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

      {filteredPropietarios.length === 0 && (
        <div className="text-center py-12">
          <User className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium text-foreground">No se encontraron propietarios</h3>
          <p className="text-muted-foreground mt-1">
            {searchTerm ? 'Intenta con otros términos de búsqueda' : 'Comienza agregando un nuevo propietario'}
          </p>
        </div>
      )}
    </div>
  );
}
