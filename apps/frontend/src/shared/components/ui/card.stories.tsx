import type { Meta, StoryObj } from '@storybook/react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from './card';
import { Button } from './button';

/**
 * Card es un contenedor versátil para agrupar contenido relacionado.
 *
 * ## Componentes
 * - `Card`: Contenedor principal
 * - `CardHeader`: Encabezado con padding
 * - `CardTitle`: Título principal
 * - `CardDescription`: Descripción secundaria
 * - `CardContent`: Área de contenido
 * - `CardFooter`: Pie con acciones
 */
const meta: Meta<typeof Card> = {
  title: 'UI/Card',
  component: Card,
  tags: ['autodocs'],
  parameters: {
    docs: {
      description: {
        component: 'Componente Card para agrupar contenido con bordes y sombra.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Card>;

/**
 * Card básica con todos los subcomponentes
 */
export const Default: Story = {
  render: () => (
    <Card className="w-[350px]">
      <CardHeader>
        <CardTitle>Card Title</CardTitle>
        <CardDescription>Card description goes here.</CardDescription>
      </CardHeader>
      <CardContent>
        <p>Card content with any component or text.</p>
      </CardContent>
      <CardFooter>
        <Button>Action</Button>
      </CardFooter>
    </Card>
  ),
};

/**
 * Card solo con contenido
 */
export const ContentOnly: Story = {
  render: () => (
    <Card className="w-[350px]">
      <CardContent className="pt-6">
        <p>Simple card with just content.</p>
      </CardContent>
    </Card>
  ),
};

/**
 * Card con formulario
 */
export const WithForm: Story = {
  render: () => (
    <Card className="w-[350px]">
      <CardHeader>
        <CardTitle>Crear cuenta</CardTitle>
        <CardDescription>Ingresa tus datos para registrarte.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <label className="text-sm font-medium">Email</label>
          <input
            type="email"
            placeholder="email@ejemplo.com"
            className="w-full rounded-md border px-3 py-2"
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">Contraseña</label>
          <input
            type="password"
            placeholder="••••••••"
            className="w-full rounded-md border px-3 py-2"
          />
        </div>
      </CardContent>
      <CardFooter className="flex justify-between">
        <Button variant="outline">Cancelar</Button>
        <Button>Crear cuenta</Button>
      </CardFooter>
    </Card>
  ),
};

/**
 * Card informativa para dashboard
 */
export const DashboardCard: Story = {
  render: () => (
    <Card className="w-[250px]">
      <CardHeader className="pb-2">
        <CardDescription>Pacientes totales</CardDescription>
        <CardTitle className="text-4xl">1,234</CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-xs text-muted-foreground">+20.1% desde el mes pasado</p>
      </CardContent>
    </Card>
  ),
};

/**
 * Grid de cards
 */
export const CardGrid: Story = {
  render: () => (
    <div className="grid grid-cols-3 gap-4">
      {[1, 2, 3].map((i) => (
        <Card key={i} className="w-[200px]">
          <CardHeader>
            <CardTitle>Card {i}</CardTitle>
            <CardDescription>Descripción</CardDescription>
          </CardHeader>
          <CardContent>
            <p>Contenido de la card {i}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  ),
};
