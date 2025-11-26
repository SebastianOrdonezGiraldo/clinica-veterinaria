import { UseFormRegister, FieldErrors } from 'react-hook-form';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';

export interface SignosVitalesData {
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  temperatura?: number;
  pesoKg?: number;
}

interface SignosVitalesFormProps {
  register: UseFormRegister<any>;
  errors: FieldErrors<any>;
}

export function SignosVitalesForm({ register, errors }: SignosVitalesFormProps) {
  return (
    <div className="grid gap-4 md:grid-cols-4">
      <div className="space-y-2">
        <Label htmlFor="frecuenciaCardiaca">FC (lpm)</Label>
        <Input
          id="frecuenciaCardiaca"
          type="number"
          min="0"
          {...register('frecuenciaCardiaca', { valueAsNumber: true })}
          placeholder="Frecuencia cardíaca"
        />
        {errors.frecuenciaCardiaca && (
          <p className="text-sm text-destructive">{errors.frecuenciaCardiaca.message as string}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="frecuenciaRespiratoria">FR (rpm)</Label>
        <Input
          id="frecuenciaRespiratoria"
          type="number"
          min="0"
          {...register('frecuenciaRespiratoria', { valueAsNumber: true })}
          placeholder="Frecuencia respiratoria"
        />
        {errors.frecuenciaRespiratoria && (
          <p className="text-sm text-destructive">{errors.frecuenciaRespiratoria.message as string}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="temperatura">Temperatura (°C)</Label>
        <Input
          id="temperatura"
          type="number"
          step="0.1"
          min="0"
          {...register('temperatura', { valueAsNumber: true })}
          placeholder="Temperatura"
        />
        {errors.temperatura && (
          <p className="text-sm text-destructive">{errors.temperatura.message as string}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="pesoKg">Peso (kg)</Label>
        <Input
          id="pesoKg"
          type="number"
          step="0.1"
          min="0"
          {...register('pesoKg', { valueAsNumber: true })}
          placeholder="Peso"
        />
        {errors.pesoKg && (
          <p className="text-sm text-destructive">{errors.pesoKg.message as string}</p>
        )}
      </div>
    </div>
  );
}

