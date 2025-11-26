import { useNavigate } from 'react-router-dom';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent } from '@shared/components/ui/card';
import {
  Dog,
  Stethoscope,
  Syringe,
  Microscope,
  Activity,
  Scissors,
  Heart,
  MapPin,
  Phone,
  Mail,
  Clock,
  Facebook,
  Instagram,
  GraduationCap,
  Scan,
  Monitor,
  Sparkles,
  Shield,
  Users,
  MessageSquare,
  Award,
  CheckCircle2,
  Crosshair,
  Eye,
  PawPrint,
} from 'lucide-react';

export default function LandingPage() {
  const navigate = useNavigate();

  const servicios = [
    {
      icon: Stethoscope,
      title: 'Consulta General y Especializada',
      description:
        'Atención médica integral con diagnósticos precisos y un enfoque personalizado.',
    },
    {
      icon: Activity,
      title: 'Exámenes Físicos',
      description:
        'Chequeos completos para evaluar el estado de salud general de tu mascota.',
    },
    {
      icon: Syringe,
      title: 'Vacunación',
      description:
        'Planes de vacunación actualizados para prevenir enfermedades.',
    },
    {
      icon: Microscope,
      title: 'Laboratorio',
      description:
        'Procesos clínicos avanzados: hematología, bioquímica, coprología, orina y más.',
    },
    {
      icon: Scan,
      title: 'Imagenología',
      description:
        'Rayos X digitales, ecografía y ultrasonido para diagnósticos confiables.',
    },
    {
      icon: Scissors,
      title: 'Cirugía General',
      description:
        'Procedimientos quirúrgicos realizados con estándares modernos y seguros.',
    },
    {
      icon: Heart,
      title: 'Hospitalización',
      description:
        'Cuidado y monitoreo constante para pacientes que requieren atención especial.',
    },
  ];

  const valores = [
    { icon: Heart, title: 'Compasión', description: 'Excelencia humana y técnica.' },
    { icon: Award, title: 'Excelencia', description: 'Atención de primer nivel.' },
    { icon: Sparkles, title: 'Innovación', description: 'Tecnología avanzada.' },
    { icon: Shield, title: 'Integridad', description: 'Honestidad y transparencia.' },
    { icon: CheckCircle2, title: 'Responsabilidad', description: 'Compromiso total.' },
    { icon: MessageSquare, title: 'Comunicación', description: 'Trato cercano.' },
    { icon: Users, title: 'Respeto', description: 'Cada vida importa.' },
    { icon: GraduationCap, title: 'Colaboración', description: 'Trabajo académico.' },
  ];

  return (
    <div className="min-h-screen bg-white scroll-smooth">
      {/* HEADER */}
      <header className="sticky top-0 z-50 w-full border-b bg-white/70 backdrop-blur-xl shadow-sm">
        <div className="container mx-auto px-4 py-3 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
              <Dog className="h-6 w-6 text-primary" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight">VetClinic Pro</h1>
              <p className="text-xs text-muted-foreground">
                Clínica Veterinaria Universitaria
              </p>
            </div>
          </div>

          <Button onClick={() => navigate('/login')} size="lg" className="font-semibold">
            Acceder
          </Button>
        </div>
      </header>

      {/* HERO */}
      <section className="container mx-auto px-4 py-20 lg:py-28 grid lg:grid-cols-2 gap-12 items-center">
        <div className="space-y-6">
          <h2 className="text-5xl font-extrabold leading-tight tracking-tight">
            Cuidado veterinario{' '}
            <span className="text-primary">de alta calidad</span>
            <br />
            para quienes más amas
          </h2>
          <p className="text-xl text-muted-foreground max-w-lg">
            Atención confiable, moderna y humana para perros y gatos. Tecnología avanzada y
            profesionales en formación que garantizan bienestar y salud.
          </p>

          <div className="flex flex-col sm:flex-row gap-4">
            <Button
              size="lg"
              className="text-lg px-8 py-6"
              onClick={() => navigate('/agendar-cita')}
            >
              Solicitar Cita
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="text-lg px-8 py-6"
              onClick={() =>
                document.getElementById('servicios')?.scrollIntoView({ behavior: 'smooth' })
              }
            >
              Ver Servicios
            </Button>
          </div>
        </div>

        {/* HERO IMAGE */}
        <div className="relative">
          <div className="aspect-square rounded-3xl bg-gradient-to-br from-primary/20 to-green-200/30 backdrop-blur-lg shadow-xl flex items-center justify-center overflow-hidden">
            <Dog className="h-40 w-40 text-primary/40" />
          </div>
        </div>
      </section>

      {/* BENEFITS STRIP */}
      <section className="bg-primary/5 py-10 border-y">
        <div className="container mx-auto px-4 grid md:grid-cols-3 gap-6 text-center">
          <div>
            <PawPrint className="h-10 w-10 text-primary mx-auto mb-2" />
            <h4 className="font-bold text-lg">+10 Años de Experiencia</h4>
          </div>
          <div>
            <Monitor className="h-10 w-10 text-primary mx-auto mb-2" />
            <h4 className="font-bold text-lg">Tecnología Avanzada</h4>
          </div>
          <div>
            <Users className="h-10 w-10 text-primary mx-auto mb-2" />
            <h4 className="font-bold text-lg">Clínica Universitaria</h4>
          </div>
        </div>
      </section>

      {/* SERVICIOS */}
      <section id="servicios" className="py-20 bg-white">
        <div className="container mx-auto px-4">
          <div className="text-center mb-14">
            <h2 className="text-4xl font-bold mb-2">Nuestros Servicios</h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Atención profesional y precios accesibles.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {servicios.map((servicio, index) => {
              const Icon = servicio.icon;
              return (
                <Card
                  key={index}
                  className="border hover:shadow-xl hover:border-primary/50 transition-all"
                >
                  <CardContent className="p-6 space-y-3">
                    <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center">
                      <Icon className="h-6 w-6 text-primary" />
                    </div>
                    <h3 className="font-semibold text-xl">{servicio.title}</h3>
                    <p className="text-muted-foreground text-sm">{servicio.description}</p>
                    <Button variant="link" className="p-0 text-primary font-semibold">
                      Saber más →
                    </Button>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* CTA INTERMEDIO */}
      <section className="py-16 bg-gradient-to-r from-primary to-blue-600 text-white text-center">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold mb-4">Agenda la salud de tu mascota hoy mismo</h2>
          <p className="mb-6 text-lg opacity-90">
            Nuestro equipo está listo para brindarte atención profesional y humana.
          </p>
          <Button
            size="lg"
            className="bg-white text-primary font-bold hover:bg-gray-100"
            onClick={() => navigate('/agendar-cita')}
          >
            Solicitar Cita
          </Button>
        </div>
      </section>

      {/* VALORES */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-4 max-w-5xl">
          <h2 className="text-4xl font-bold text-center mb-12">Nuestros Valores</h2>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {valores.map((valor, index) => {
              const Icon = valor.icon;
              return (
                <Card key={index} className="text-center hover:shadow-lg transition-all">
                  <CardContent className="p-6 space-y-3">
                    <div className="h-12 w-12 rounded-full bg-primary/10 mx-auto flex justify-center items-center">
                      <Icon className="h-6 w-6 text-primary" />
                    </div>
                    <h4 className="font-semibold">{valor.title}</h4>
                    <p className="text-sm text-muted-foreground">{valor.description}</p>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* CONTACTO */}
      <section id="contacto" className="py-20 bg-primary/5">
        <div className="container mx-auto px-4 grid lg:grid-cols-2 gap-12">
          <div className="space-y-8">
            <h2 className="text-4xl font-bold">Contacto y Ubicación</h2>
            <p className="text-lg text-muted-foreground">
              Estamos aquí para cuidar de tu mascota.
            </p>

            <Card>
              <CardContent className="p-6 flex gap-4">
                <MapPin className="h-6 w-6 text-primary" />
                <div>
                  <h3 className="font-semibold">Dirección</h3>
                  <p className="text-muted-foreground">Calle 6 Norte # 14-26, [Ciudad]</p>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-6 flex gap-4">
                <Phone className="h-6 w-6 text-primary" />
                <div>
                  <h3 className="font-semibold">Teléfono / WhatsApp</h3>
                  <p className="text-muted-foreground">+57 XXX XXX XXXX</p>
                </div>
              </CardContent>
            </Card>

            <Button
              className="w-full py-6 text-lg"
              size="lg"
              onClick={() => navigate('/agendar-cita')}
            >
              Solicitar Cita
            </Button>
          </div>

          <Card>
            <CardContent className="aspect-square bg-primary/10 flex items-center justify-center">
              <MapPin className="h-20 w-20 text-primary/40" />
            </CardContent>
          </Card>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="bg-foreground text-white py-10 mt-10">
        <div className="container mx-auto px-4 grid md:grid-cols-3 gap-8">
          <div>
            <h3 className="font-bold text-lg mb-3">VetClinic Pro</h3>
            <p className="text-gray-300 text-sm">
              Cuidando vidas, formando profesionales.
            </p>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Contacto</h4>
            <p className="text-gray-300 text-sm">+573186160630</p>
            <p className="text-gray-300 text-sm">contacto@vetclinic.com</p>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Síguenos</h4>
            <div className="flex gap-4">
              <Facebook className="h-5 w-5 cursor-pointer" />
              <Instagram className="h-5 w-5 cursor-pointer" />
            </div>
          </div>
        </div>

        <p className="text-center text-gray-400 text-sm mt-6">
          © {new Date().getFullYear()} VetClinic Pro — Todos los derechos reservados.
        </p>
      </footer>
    </div>
  );
}
