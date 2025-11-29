import type { Meta, StoryObj } from '@storybook/react';
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from './select';

/**
 * Select es un componente de selección accesible basado en Radix UI.
 *
 * ## Componentes
 * - `Select`: Contenedor principal
 * - `SelectTrigger`: Botón que muestra el valor seleccionado
 * - `SelectValue`: Valor o placeholder visible
 * - `SelectContent`: Contenedor del dropdown
 * - `SelectGroup`: Grupo de opciones
 * - `SelectLabel`: Etiqueta para grupos
 * - `SelectItem`: Opción individual
 */
const meta: Meta<typeof Select> = {
  title: 'UI/Select',
  component: Select,
  tags: ['autodocs'],
  parameters: {
    docs: {
      description: {
        component: 'Componente de selección accesible con soporte para grupos y teclado.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Select>;

/**
 * Select básico
 */
export const Default: Story = {
  render: () => (
    <Select>
      <SelectTrigger className="w-[180px]">
        <SelectValue placeholder="Seleccionar especie" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="canino">Canino</SelectItem>
        <SelectItem value="felino">Felino</SelectItem>
        <SelectItem value="ave">Ave</SelectItem>
        <SelectItem value="exotico">Exótico</SelectItem>
      </SelectContent>
    </Select>
  ),
};

/**
 * Select con grupos
 */
export const WithGroups: Story = {
  render: () => (
    <Select>
      <SelectTrigger className="w-[200px]">
        <SelectValue placeholder="Seleccionar veterinario" />
      </SelectTrigger>
      <SelectContent>
        <SelectGroup>
          <SelectLabel>Medicina General</SelectLabel>
          <SelectItem value="dr-garcia">Dr. García</SelectItem>
          <SelectItem value="dr-martinez">Dr. Martínez</SelectItem>
        </SelectGroup>
        <SelectGroup>
          <SelectLabel>Cirugía</SelectLabel>
          <SelectItem value="dr-lopez">Dr. López</SelectItem>
          <SelectItem value="dr-rodriguez">Dr. Rodríguez</SelectItem>
        </SelectGroup>
      </SelectContent>
    </Select>
  ),
};

/**
 * Select de estado de cita
 */
export const CitaEstado: Story = {
  render: () => (
    <Select defaultValue="pendiente">
      <SelectTrigger className="w-[180px]">
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="pendiente">Pendiente</SelectItem>
        <SelectItem value="confirmada">Confirmada</SelectItem>
        <SelectItem value="en-curso">En Curso</SelectItem>
        <SelectItem value="completada">Completada</SelectItem>
        <SelectItem value="cancelada">Cancelada</SelectItem>
      </SelectContent>
    </Select>
  ),
};

/**
 * Select deshabilitado
 */
export const Disabled: Story = {
  render: () => (
    <Select disabled>
      <SelectTrigger className="w-[180px]">
        <SelectValue placeholder="No disponible" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="1">Opción 1</SelectItem>
        <SelectItem value="2">Opción 2</SelectItem>
      </SelectContent>
    </Select>
  ),
};

/**
 * Select con valor por defecto
 */
export const WithDefaultValue: Story = {
  render: () => (
    <Select defaultValue="canino">
      <SelectTrigger className="w-[180px]">
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="canino">Canino</SelectItem>
        <SelectItem value="felino">Felino</SelectItem>
        <SelectItem value="ave">Ave</SelectItem>
      </SelectContent>
    </Select>
  ),
};

/**
 * Select en formulario
 */
export const InForm: Story = {
  render: () => (
    <div className="w-[300px] space-y-4">
      <div className="space-y-2">
        <label className="text-sm font-medium">Especie</label>
        <Select>
          <SelectTrigger>
            <SelectValue placeholder="Seleccionar especie" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="canino">Canino</SelectItem>
            <SelectItem value="felino">Felino</SelectItem>
            <SelectItem value="ave">Ave</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">Sexo</label>
        <Select>
          <SelectTrigger>
            <SelectValue placeholder="Seleccionar sexo" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="M">Macho</SelectItem>
            <SelectItem value="H">Hembra</SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>
  ),
};
