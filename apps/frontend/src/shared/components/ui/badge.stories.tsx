import type { Meta, StoryObj } from '@storybook/react';
import { Badge } from './badge';

/**
 * Badge es un componente para mostrar etiquetas, estados o categorías.
 *
 * ## Variantes
 * - `default`: Fondo primario
 * - `secondary`: Fondo secundario
 * - `destructive`: Para alertas o errores
 * - `outline`: Solo borde, sin fondo
 */
const meta: Meta<typeof Badge> = {
  title: 'UI/Badge',
  component: Badge,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['default', 'secondary', 'destructive', 'outline'],
      description: 'Variante visual del badge',
    },
  },
  parameters: {
    docs: {
      description: {
        component: 'Etiqueta para mostrar estados, categorías o contadores.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Badge>;

/**
 * Badge por defecto
 */
export const Default: Story = {
  args: {
    children: 'Badge',
  },
};

/**
 * Badge secundario
 */
export const Secondary: Story = {
  args: {
    variant: 'secondary',
    children: 'Secondary',
  },
};

/**
 * Badge destructivo para alertas
 */
export const Destructive: Story = {
  args: {
    variant: 'destructive',
    children: 'Error',
  },
};

/**
 * Badge outline
 */
export const Outline: Story = {
  args: {
    variant: 'outline',
    children: 'Outline',
  },
};

/**
 * Estados de cita
 */
export const CitaStatuses: Story = {
  render: () => (
    <div className="flex gap-2">
      <Badge variant="default">Confirmada</Badge>
      <Badge variant="secondary">Pendiente</Badge>
      <Badge variant="outline">En espera</Badge>
      <Badge variant="destructive">Cancelada</Badge>
    </div>
  ),
};

/**
 * Especies de pacientes
 */
export const EspeciesBadges: Story = {
  render: () => (
    <div className="flex gap-2">
      <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">
        Canino
      </Badge>
      <Badge variant="outline" className="bg-purple-50 text-purple-700 border-purple-200">
        Felino
      </Badge>
      <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">
        Ave
      </Badge>
      <Badge variant="outline" className="bg-orange-50 text-orange-700 border-orange-200">
        Exótico
      </Badge>
    </div>
  ),
};

/**
 * Badge con número (contador)
 */
export const WithCount: Story = {
  render: () => (
    <div className="flex items-center gap-2">
      <span>Notificaciones</span>
      <Badge variant="destructive">5</Badge>
    </div>
  ),
};

/**
 * Todas las variantes
 */
export const AllVariants: Story = {
  render: () => (
    <div className="flex gap-2">
      <Badge variant="default">Default</Badge>
      <Badge variant="secondary">Secondary</Badge>
      <Badge variant="destructive">Destructive</Badge>
      <Badge variant="outline">Outline</Badge>
    </div>
  ),
};
