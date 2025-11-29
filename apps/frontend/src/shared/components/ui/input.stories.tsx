import type { Meta, StoryObj } from '@storybook/react';
import { Input } from './input';

/**
 * Input es un campo de entrada de texto con estilos consistentes.
 *
 * ## Características
 * - Soporta todos los tipos de input HTML
 * - Estados de focus, disabled y validación
 * - Compatible con react-hook-form
 * - Estilos de Tailwind CSS
 */
const meta: Meta<typeof Input> = {
  title: 'UI/Input',
  component: Input,
  tags: ['autodocs'],
  argTypes: {
    type: {
      control: 'select',
      options: ['text', 'email', 'password', 'number', 'tel', 'url', 'search'],
      description: 'Tipo de input',
    },
    placeholder: {
      control: 'text',
      description: 'Texto placeholder',
    },
    disabled: {
      control: 'boolean',
      description: 'Si está deshabilitado',
    },
  },
  parameters: {
    docs: {
      description: {
        component: 'Campo de entrada de texto estilizado compatible con formularios.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Input>;

/**
 * Input por defecto
 */
export const Default: Story = {
  args: {
    placeholder: 'Escribe aquí...',
  },
};

/**
 * Input de email
 */
export const Email: Story = {
  args: {
    type: 'email',
    placeholder: 'email@ejemplo.com',
  },
};

/**
 * Input de contraseña
 */
export const Password: Story = {
  args: {
    type: 'password',
    placeholder: '••••••••',
  },
};

/**
 * Input numérico
 */
export const Number: Story = {
  args: {
    type: 'number',
    placeholder: '0',
  },
};

/**
 * Input de búsqueda
 */
export const Search: Story = {
  args: {
    type: 'search',
    placeholder: 'Buscar...',
  },
};

/**
 * Input deshabilitado
 */
export const Disabled: Story = {
  args: {
    disabled: true,
    placeholder: 'Deshabilitado',
  },
};

/**
 * Input con valor
 */
export const WithValue: Story = {
  args: {
    defaultValue: 'Valor inicial',
  },
};

/**
 * Input con label
 */
export const WithLabel: Story = {
  render: () => (
    <div className="space-y-2">
      <label className="text-sm font-medium">Email</label>
      <Input type="email" placeholder="email@ejemplo.com" />
    </div>
  ),
};

/**
 * Input con error
 */
export const WithError: Story = {
  render: () => (
    <div className="space-y-2">
      <label className="text-sm font-medium">Email</label>
      <Input
        type="email"
        placeholder="email@ejemplo.com"
        className="border-red-500 focus-visible:ring-red-500"
      />
      <p className="text-sm text-red-500">Email inválido</p>
    </div>
  ),
};

/**
 * Formulario con múltiples inputs
 */
export const FormExample: Story = {
  render: () => (
    <form className="w-[350px] space-y-4">
      <div className="space-y-2">
        <label className="text-sm font-medium">Nombre</label>
        <Input placeholder="Juan Pérez" />
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">Email</label>
        <Input type="email" placeholder="juan@ejemplo.com" />
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">Teléfono</label>
        <Input type="tel" placeholder="+57 300 123 4567" />
      </div>
    </form>
  ),
};
