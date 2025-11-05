import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { getPropietarioById, savePropietario } from '@/lib/mockData';
import { Propietario } from '@/types';

export default function PropietarioForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = id && id !== 'nuevo';

  const [formData, setFormData] = useState<Partial<Propietario>>({
    nombre: '',
    documento: '',
    email: '',
    telefono: '',
    direccion: '',
  });

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isEdit) {
      const propietario = getPropietarioById(id);
      if (propietario) {
        setFormData({
          nombre: propietario.nombre,
          documento: propietario.documento || '',
          email: propietario.email || '',
          telefono: propietario.telefono || '',
          direccion: propietario.direccion || '',
        });
      } else {
        toast.error('Propietario no encontrado');
        navigate('/propietarios');
      }
    }
  }, [id, isEdit, navigate]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.nombre?.trim()) {
      newErrors.nombre = 'El nombre es obligatorio';
    }

    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Email inválido';
    }

    if (formData.telefono && !/^[\d\s\-+()]+$/.test(formData.telefono)) {
      newErrors.telefono = 'Teléfono inválido';
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

      const propietarioData = {
        ...(isEdit && { id }),
        nombre: formData.nombre!,
        documento: formData.documento,
        email: formData.email,
        telefono: formData.telefono,
        direccion: formData.direccion,
      };

      savePropietario(propietarioData);

      toast.success(isEdit ? 'Propietario actualizado correctamente' : 'Propietario creado correctamente');
      navigate('/propietarios');
    } catch (error) {
      toast.error('Error al guardar el propietario');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: keyof Propietario, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Limpiar error del campo cuando el usuario empieza a escribir
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/propietarios')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Propietario' : 'Nuevo Propietario'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEdit ? 'Actualiza la información del propietario' : 'Registra un nuevo propietario en el sistema'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>Información del Propietario</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-6 md:grid-cols-2">
              {/* Nombre */}
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="nombre">
                  Nombre Completo <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="nombre"
                  value={formData.nombre}
                  onChange={(e) => handleChange('nombre', e.target.value)}
                  placeholder="Ej: Juan Pérez García"
                  className={errors.nombre ? 'border-destructive' : ''}
                />
                {errors.nombre && (
                  <p className="text-sm text-destructive">{errors.nombre}</p>
                )}
              </div>

              {/* Documento */}
              <div className="space-y-2">
                <Label htmlFor="documento">Documento de Identidad</Label>
                <Input
                  id="documento"
                  value={formData.documento}
                  onChange={(e) => handleChange('documento', e.target.value)}
                  placeholder="Ej: 12345678"
                />
              </div>

              {/* Teléfono */}
              <div className="space-y-2">
                <Label htmlFor="telefono">Teléfono</Label>
                <Input
                  id="telefono"
                  value={formData.telefono}
                  onChange={(e) => handleChange('telefono', e.target.value)}
                  placeholder="Ej: 555-0101"
                  className={errors.telefono ? 'border-destructive' : ''}
                />
                {errors.telefono && (
                  <p className="text-sm text-destructive">{errors.telefono}</p>
                )}
              </div>

              {/* Email */}
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="email">Correo Electrónico</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => handleChange('email', e.target.value)}
                  placeholder="Ej: juan@email.com"
                  className={errors.email ? 'border-destructive' : ''}
                />
                {errors.email && (
                  <p className="text-sm text-destructive">{errors.email}</p>
                )}
              </div>

              {/* Dirección */}
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="direccion">Dirección</Label>
                <Input
                  id="direccion"
                  value={formData.direccion}
                  onChange={(e) => handleChange('direccion', e.target.value)}
                  placeholder="Ej: Calle Principal 123, Ciudad"
                />
              </div>
            </div>

            {/* Botones */}
            <div className="flex gap-3 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/propietarios')}
                disabled={loading}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={loading} className="gap-2">
                <Save className="h-4 w-4" />
                {loading ? 'Guardando...' : isEdit ? 'Actualizar' : 'Crear Propietario'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}

