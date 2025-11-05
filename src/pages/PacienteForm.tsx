import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import { getPacienteById, savePaciente, mockPropietarios } from '@/lib/mockData';
import { Paciente } from '@/types';

export default function PacienteForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = id && id !== 'nuevo';

  const [formData, setFormData] = useState<Partial<Paciente>>({
    nombre: '',
    especie: 'Canino',
    raza: '',
    sexo: 'M',
    edadMeses: undefined,
    pesoKg: undefined,
    propietarioId: '',
    microchip: '',
    notas: '',
  });

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isEdit) {
      const paciente = getPacienteById(id);
      if (paciente) {
        setFormData({
          nombre: paciente.nombre,
          especie: paciente.especie,
          raza: paciente.raza || '',
          sexo: paciente.sexo,
          edadMeses: paciente.edadMeses,
          pesoKg: paciente.pesoKg,
          propietarioId: paciente.propietarioId,
          microchip: paciente.microchip || '',
          notas: paciente.notas || '',
        });
      } else {
        toast.error('Paciente no encontrado');
        navigate('/pacientes');
      }
    }
  }, [id, isEdit, navigate]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.nombre?.trim()) {
      newErrors.nombre = 'El nombre es obligatorio';
    }

    if (!formData.propietarioId) {
      newErrors.propietarioId = 'Debe seleccionar un propietario';
    }

    if (formData.edadMeses && formData.edadMeses < 0) {
      newErrors.edadMeses = 'La edad no puede ser negativa';
    }

    if (formData.pesoKg && formData.pesoKg <= 0) {
      newErrors.pesoKg = 'El peso debe ser mayor a 0';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Por favor corrige los errores del formulario');
      return;
    }

    setLoading(true);

    try {
      // Simular delay de red
      await new Promise(resolve => setTimeout(resolve, 500));

      const pacienteData = {
        ...(isEdit && { id }),
        nombre: formData.nombre!,
        especie: formData.especie as 'Canino' | 'Felino' | 'Otro',
        raza: formData.raza,
        sexo: formData.sexo as 'M' | 'F',
        edadMeses: formData.edadMeses,
        pesoKg: formData.pesoKg,
        propietarioId: formData.propietarioId!,
        microchip: formData.microchip,
        notas: formData.notas,
      };

      savePaciente(pacienteData);

      toast.success(isEdit ? 'Paciente actualizado correctamente' : 'Paciente creado correctamente');
      navigate('/pacientes');
    } catch (error) {
      toast.error('Error al guardar el paciente');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: keyof Paciente, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Limpiar error del campo cuando el usuario empieza a escribir
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/pacientes')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Paciente' : 'Nuevo Paciente'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEdit ? 'Actualiza la información del paciente' : 'Registra un nuevo paciente en el sistema'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>Información del Paciente</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-6 md:grid-cols-2">
              {/* Nombre */}
              <div className="space-y-2">
                <Label htmlFor="nombre">
                  Nombre <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="nombre"
                  value={formData.nombre}
                  onChange={(e) => handleChange('nombre', e.target.value)}
                  placeholder="Ej: Max, Luna, Rocky"
                  className={errors.nombre ? 'border-destructive' : ''}
                />
                {errors.nombre && (
                  <p className="text-sm text-destructive">{errors.nombre}</p>
                )}
              </div>

              {/* Propietario */}
              <div className="space-y-2">
                <Label htmlFor="propietario">
                  Propietario <span className="text-destructive">*</span>
                </Label>
                <Select
                  value={formData.propietarioId}
                  onValueChange={(value) => handleChange('propietarioId', value)}
                >
                  <SelectTrigger className={errors.propietarioId ? 'border-destructive' : ''}>
                    <SelectValue placeholder="Selecciona un propietario" />
                  </SelectTrigger>
                  <SelectContent>
                    {mockPropietarios.map((prop) => (
                      <SelectItem key={prop.id} value={prop.id}>
                        {prop.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.propietarioId && (
                  <p className="text-sm text-destructive">{errors.propietarioId}</p>
                )}
              </div>

              {/* Especie */}
              <div className="space-y-2">
                <Label htmlFor="especie">Especie</Label>
                <Select
                  value={formData.especie}
                  onValueChange={(value) => handleChange('especie', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Canino">Canino</SelectItem>
                    <SelectItem value="Felino">Felino</SelectItem>
                    <SelectItem value="Otro">Otro</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Raza */}
              <div className="space-y-2">
                <Label htmlFor="raza">Raza</Label>
                <Input
                  id="raza"
                  value={formData.raza}
                  onChange={(e) => handleChange('raza', e.target.value)}
                  placeholder="Ej: Golden Retriever, Siamés"
                />
              </div>

              {/* Sexo */}
              <div className="space-y-2">
                <Label htmlFor="sexo">Sexo</Label>
                <Select
                  value={formData.sexo}
                  onValueChange={(value) => handleChange('sexo', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="M">Macho</SelectItem>
                    <SelectItem value="F">Hembra</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Edad en meses */}
              <div className="space-y-2">
                <Label htmlFor="edadMeses">Edad (meses)</Label>
                <Input
                  id="edadMeses"
                  type="number"
                  min="0"
                  value={formData.edadMeses || ''}
                  onChange={(e) => handleChange('edadMeses', e.target.value ? parseInt(e.target.value) : undefined)}
                  placeholder="Ej: 24"
                  className={errors.edadMeses ? 'border-destructive' : ''}
                />
                {errors.edadMeses && (
                  <p className="text-sm text-destructive">{errors.edadMeses}</p>
                )}
              </div>

              {/* Peso */}
              <div className="space-y-2">
                <Label htmlFor="pesoKg">Peso (kg)</Label>
                <Input
                  id="pesoKg"
                  type="number"
                  step="0.1"
                  min="0"
                  value={formData.pesoKg || ''}
                  onChange={(e) => handleChange('pesoKg', e.target.value ? parseFloat(e.target.value) : undefined)}
                  placeholder="Ej: 28.5"
                  className={errors.pesoKg ? 'border-destructive' : ''}
                />
                {errors.pesoKg && (
                  <p className="text-sm text-destructive">{errors.pesoKg}</p>
                )}
              </div>

              {/* Microchip */}
              <div className="space-y-2">
                <Label htmlFor="microchip">Microchip</Label>
                <Input
                  id="microchip"
                  value={formData.microchip}
                  onChange={(e) => handleChange('microchip', e.target.value)}
                  placeholder="Ej: MX001"
                />
              </div>
            </div>

            {/* Notas */}
            <div className="space-y-2">
              <Label htmlFor="notas">Notas adicionales</Label>
              <Textarea
                id="notas"
                value={formData.notas}
                onChange={(e) => handleChange('notas', e.target.value)}
                placeholder="Información adicional sobre el paciente..."
                rows={4}
              />
            </div>

            {/* Botones */}
            <div className="flex gap-3 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/pacientes')}
                disabled={loading}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={loading} className="gap-2">
                <Save className="h-4 w-4" />
                {loading ? 'Guardando...' : isEdit ? 'Actualizar' : 'Crear Paciente'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}

