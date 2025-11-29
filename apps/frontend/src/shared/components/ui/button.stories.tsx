import type { Meta, StoryObj } from '@storybook/react';
import { Plus, Loader2, Trash2, Mail } from 'lucide-react';
import { Button } from './button';

/**
 * Button es un componente fundamental de la UI que soporta múltiples variantes
 * y tamaños para diferentes casos de uso.
 *
 * ## Características
 * - 6 variantes visuales: default, destructive, outline, secondary, ghost, link
 * - 4 tamaños: default, sm, lg, icon
 * - Soporte para modo `asChild` usando Radix Slot
 * - Estados disabled automáticos
 * - Animaciones de transición suaves
 */
const meta: Meta<typeof Button> = {
  title: 'UI/Button',
  component: Button,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['default', 'destructive', 'outline', 'secondary', 'ghost', 'link'],
      description: 'Variante visual del botón',
      table: {
        defaultValue: { summary: 'default' },
      },
    },
    size: {
      control: 'select',
      options: ['default', 'sm', 'lg', 'icon'],
      description: 'Tamaño del botón',
      table: {
        defaultValue: { summary: 'default' },
      },
    },
    disabled: {
      control: 'boolean',
      description: 'Si el botón está deshabilitado',
    },
    asChild: {
      control: 'boolean',
      description: 'Renderiza el hijo como elemento principal (usando Radix Slot)',
    },
  },
  parameters: {
    docs: {
      description: {
        component: 'Botón reutilizable con múltiples variantes y tamaños basado en shadcn/ui.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Button>;

/**
 * Botón por defecto con estilo primario
 */
export const Default: Story = {
  args: {
    children: 'Button',
  },
};

/**
 * Botón primario - acción principal
 */
export const Primary: Story = {
  args: {
    variant: 'default',
    children: 'Primary Button',
  },
};

/**
 * Botón destructivo - para acciones peligrosas como eliminar
 */
export const Destructive: Story = {
  args: {
    variant: 'destructive',
    children: 'Eliminar',
  },
};

/**
 * Botón outline - acción secundaria con borde
 */
export const Outline: Story = {
  args: {
    variant: 'outline',
    children: 'Outline Button',
  },
};

/**
 * Botón secundario - acción alternativa
 */
export const Secondary: Story = {
  args: {
    variant: 'secondary',
    children: 'Secondary Button',
  },
};

/**
 * Botón ghost - sin fondo, solo hover
 */
export const Ghost: Story = {
  args: {
    variant: 'ghost',
    children: 'Ghost Button',
  },
};

/**
 * Botón link - estilo de enlace
 */
export const Link: Story = {
  args: {
    variant: 'link',
    children: 'Link Button',
  },
};

/**
 * Botón pequeño
 */
export const Small: Story = {
  args: {
    size: 'sm',
    children: 'Small Button',
  },
};

/**
 * Botón grande
 */
export const Large: Story = {
  args: {
    size: 'lg',
    children: 'Large Button',
  },
};

/**
 * Botón con icono - tamaño cuadrado para iconos
 */
export const Icon: Story = {
  args: {
    size: 'icon',
    variant: 'outline',
    children: <Plus className="h-4 w-4" />,
  },
};

/**
 * Botón con icono y texto
 */
export const WithIcon: Story = {
  args: {
    children: (
      <>
        <Plus className="mr-2 h-4 w-4" />
        Agregar
      </>
    ),
  },
};

/**
 * Botón de carga - muestra spinner mientras procesa
 */
export const Loading: Story = {
  args: {
    disabled: true,
    children: (
      <>
        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
        Cargando...
      </>
    ),
  },
};

/**
 * Botón deshabilitado
 */
export const Disabled: Story = {
  args: {
    disabled: true,
    children: 'Deshabilitado',
  },
};

/**
 * Botón destructivo con icono
 */
export const DestructiveWithIcon: Story = {
  args: {
    variant: 'destructive',
    children: (
      <>
        <Trash2 className="mr-2 h-4 w-4" />
        Eliminar
      </>
    ),
  },
};

/**
 * Galería de todas las variantes
 */
export const AllVariants: Story = {
  render: () => (
    <div className="flex flex-wrap gap-4">
      <Button variant="default">Default</Button>
      <Button variant="destructive">Destructive</Button>
      <Button variant="outline">Outline</Button>
      <Button variant="secondary">Secondary</Button>
      <Button variant="ghost">Ghost</Button>
      <Button variant="link">Link</Button>
    </div>
  ),
};

/**
 * Galería de todos los tamaños
 */
export const AllSizes: Story = {
  render: () => (
    <div className="flex items-center gap-4">
      <Button size="sm">Small</Button>
      <Button size="default">Default</Button>
      <Button size="lg">Large</Button>
      <Button size="icon"><Mail className="h-4 w-4" /></Button>
    </div>
  ),
};
