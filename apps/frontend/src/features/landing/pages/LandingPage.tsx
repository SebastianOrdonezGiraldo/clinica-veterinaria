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
} from 'lucide-react';

export default function LandingPage() {
  const navigate = useNavigate();

  const servicios = [
    {
      icon: Stethoscope,
      title: 'Consulta General y Especializada',
      description: 'Atención médica integral para felinos y caninos con profesionales altamente capacitados.',
    },
    {
      icon: Activity,
      title: 'Exámenes Físicos',
      description: 'Evaluación completa del estado de salud de tu mascota con equipos de última generación.',
    },
    {
      icon: Syringe,
      title: 'Vacunación',
      description: 'Programas de vacunación personalizados según la edad y necesidades de tu mascota.',
    },
    {
      icon: Microscope,
      title: 'Laboratorio',
      description: 'Análisis completos: hematología, bioquímica, coprología, orina y más.',
    },
    {
      icon: Scan,
      title: 'Imagenología',
      description: 'Radiografía digital y ecografía para diagnósticos precisos y rápidos.',
    },
    {
      icon: Scissors,
      title: 'Cirugía General',
      description: 'Procedimientos quirúrgicos con técnicas modernas y equipamiento avanzado.',
    },
    {
      icon: Heart,
      title: 'Hospitalización',
      description: 'Cuidado intensivo y monitoreo constante para pacientes que requieren atención especializada.',
    },
  ];

  const equipos = [
    {
      name: 'Rayos X Digitales',
      description: 'Tecnología digital de última generación',
    },
    {
      name: 'Ecógrafo',
      description: 'Diagnóstico por imágenes de alta resolución',
    },
    {
      name: 'Mesa Quirúrgica',
      description: 'Equipamiento profesional para cirugías',
    },
    {
      name: 'Monitor de Signos Vitales',
      description: 'Monitoreo continuo durante procedimientos',
    },
    {
      name: 'Anestesia',
      description: 'Sistemas de anestesia seguros y modernos',
    },
    {
      name: 'Laboratorio',
      description: 'Equipos de análisis clínico completos',
    },
  ];

  const valores = [
    { icon: Heart, title: 'Compasión', description: 'Cuidado empático hacia cada mascota' },
    { icon: Award, title: 'Excelencia', description: 'Estándares altos en cada atención' },
    { icon: Sparkles, title: 'Innovación', description: 'Tecnología y técnicas actualizadas' },
    { icon: Shield, title: 'Integridad', description: 'Transparencia y honestidad siempre' },
    { icon: CheckCircle2, title: 'Responsabilidad', description: 'Compromiso con el bienestar animal' },
    { icon: MessageSquare, title: 'Comunicación', description: 'Diálogo claro con propietarios' },
    { icon: Users, title: 'Respeto', description: 'Valoración de cada vida animal' },
    { icon: GraduationCap, title: 'Colaboración', description: 'Trabajo en equipo multidisciplinario' },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
      {/* Header/Navigation */}
      <header className="sticky top-0 z-50 w-full border-b bg-white/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                <Dog className="h-6 w-6 text-primary" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-foreground">VetClinic Pro</h1>
                <p className="text-xs text-muted-foreground">Clínica Veterinaria Universitaria</p>
              </div>
            </div>
            <Button onClick={() => navigate('/login')} size="lg" className="font-semibold">
              Acceder al Sistema
            </Button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20 lg:py-32">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <h2 className="text-4xl lg:text-5xl font-bold text-foreground leading-tight">
              Atención veterinaria de{' '}
              <span className="text-primary">alta calidad</span> para tus animales de compañía
            </h2>
            <p className="text-xl text-muted-foreground">
              Atención integral y asequible para felinos y caninos de tu región
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Button
                size="lg"
                className="text-lg px-8 py-6"
                onClick={() => {
                  navigate('/agendar-cita');
                }}
              >
                Solicitar Cita
              </Button>
              <Button
                size="lg"
                variant="outline"
                className="text-lg px-8 py-6"
                onClick={() => {
                  document.getElementById('servicios')?.scrollIntoView({ behavior: 'smooth' });
                }}
              >
                Ver Servicios
              </Button>
            </div>
          </div>
          <div className="relative">
            {/* Placeholder para imagen de portada */}
            <div className="aspect-square rounded-2xl bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center overflow-hidden">
              <div className="text-center p-8">
                <Dog className="h-32 w-32 text-primary/40 mx-auto mb-4" />
                <p className="text-muted-foreground text-sm">
                  {/* TODO: Reemplazar con imagen real de mascotas felices / veterinario con mascota */}
                  Imagen de portada: Mascotas felices / Veterinario con mascota
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Servicios Section */}
      <section id="servicios" className="bg-white py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl lg:text-4xl font-bold text-foreground mb-4">
              Nuestros Servicios
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Servicios profesionales a precios asequibles
            </p>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {servicios.map((servicio, index) => {
              const Icon = servicio.icon;
              return (
                <Card key={index} className="border-2 hover:border-primary/50 transition-colors">
                  <CardContent className="p-6">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
                      <Icon className="h-6 w-6 text-primary" />
                    </div>
                    <h3 className="text-xl font-semibold mb-2">{servicio.title}</h3>
                    <p className="text-muted-foreground mb-4">{servicio.description}</p>
                    <Button variant="ghost" size="sm" className="text-primary">
                      Saber más →
                    </Button>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* Infraestructura / Tecnología Section */}
      <section className="bg-gradient-to-br from-primary/5 to-secondary/5 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl lg:text-4xl font-bold text-foreground mb-4">
              Equipos de Última Tecnología
            </h2>
            <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
              Contamos con tecnología moderna para asegurar diagnósticos precisos y atención de
              calidad
            </p>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
            {equipos.map((equipo, index) => (
              <Card key={index} className="overflow-hidden">
                <div className="aspect-video bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center">
                  <Monitor className="h-16 w-16 text-primary/40" />
                  {/* TODO: Reemplazar con imagen real del equipo */}
                </div>
                <CardContent className="p-6">
                  <h3 className="text-lg font-semibold mb-2">{equipo.name}</h3>
                  <p className="text-sm text-muted-foreground">{equipo.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Quiénes Somos / Filosofía Section */}
      <section className="bg-white py-20">
        <div className="container mx-auto px-4">
          <div className="max-w-4xl mx-auto space-y-12">
            <div className="text-center">
              <h2 className="text-3xl lg:text-4xl font-bold text-foreground mb-4">
                Quiénes Somos
              </h2>
              <p className="text-lg text-muted-foreground">
                Somos una clínica veterinaria universitaria comprometida con el bienestar animal,
                la educación y el servicio a la comunidad
              </p>
            </div>

            <div className="grid md:grid-cols-2 gap-8">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                      <Crosshair className="h-5 w-5 text-primary" />
                    </div>
                    <h3 className="text-2xl font-bold">Misión</h3>
                  </div>
                  <p className="text-muted-foreground">
                    Brindar atención integral y personalizada, priorizando el bienestar y la calidad
                    de vida de las mascotas, mientras formamos profesionales comprometidos con la
                    excelencia veterinaria.
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="h-10 w-10 rounded-full bg-secondary/10 flex items-center justify-center">
                      <Eye className="h-5 w-5 text-secondary" />
                    </div>
                    <h3 className="text-2xl font-bold">Visión</h3>
                  </div>
                  <p className="text-muted-foreground">
                    Ser la clínica veterinaria líder en la región, reconocida por nuestra excelencia,
                    innovación y compromiso con la comunidad, siendo referente en formación
                    académica y atención de calidad.
                  </p>
                </CardContent>
              </Card>
            </div>

            <div>
              <h3 className="text-2xl font-bold text-center mb-8">Nuestros Valores</h3>
              <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
                {valores.map((valor, index) => {
                  const Icon = valor.icon;
                  return (
                    <Card key={index} className="text-center">
                      <CardContent className="p-6">
                        <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-3">
                          <Icon className="h-6 w-6 text-primary" />
                        </div>
                        <h4 className="font-semibold mb-1">{valor.title}</h4>
                        <p className="text-sm text-muted-foreground">{valor.description}</p>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Prácticas Académicas Section */}
      <section className="bg-gradient-to-br from-primary/5 to-secondary/5 py-20">
        <div className="container mx-auto px-4">
          <Card className="max-w-4xl mx-auto">
            <CardContent className="p-8 md:p-12">
              <div className="flex items-center gap-4 mb-6">
                <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
                  <GraduationCap className="h-8 w-8 text-primary" />
                </div>
                <div>
                  <h2 className="text-3xl font-bold text-foreground">Prácticas Académicas</h2>
                  <p className="text-muted-foreground">
                    Formando la próxima generación de veterinarios
                  </p>
                </div>
              </div>
              <p className="text-lg text-muted-foreground">
                Como clínica universitaria, somos sede de prácticas profesionales para estudiantes
                de medicina veterinaria. Ofrecemos un ambiente de aprendizaje donde los futuros
                profesionales pueden desarrollar sus habilidades bajo la supervisión de
                veterinarios experimentados, combinando la excelencia académica con la atención
                práctica de calidad.
              </p>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Contacto / Ubicación Section */}
      <section id="contacto" className="bg-white py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl lg:text-4xl font-bold text-foreground mb-4">
              Contacto y Ubicación
            </h2>
            <p className="text-xl text-muted-foreground">Estamos aquí para cuidar de tu mascota</p>
          </div>

          <div className="grid lg:grid-cols-2 gap-12">
            <div className="space-y-8">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                      <MapPin className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="font-semibold text-lg mb-2">Dirección</h3>
                      <p className="text-muted-foreground">
                        {/* TODO: Actualizar con la dirección real */}
                        Calle 6 Norte # 14-26, [Ciudad]
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                      <Phone className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="font-semibold text-lg mb-2">Teléfono / WhatsApp</h3>
                      <p className="text-muted-foreground">
                        {/* TODO: Actualizar con el teléfono real */}
                        +57 XXX XXX XXXX
                      </p>
                      <Button variant="link" className="p-0 h-auto mt-2 text-primary">
                        Enviar mensaje por WhatsApp
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                      <Mail className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="font-semibold text-lg mb-2">Correo Electrónico</h3>
                      <p className="text-muted-foreground">
                        {/* TODO: Actualizar con el correo real */}
                        contacto@vetclinic.com
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                      <Clock className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="font-semibold text-lg mb-2">Horarios de Atención</h3>
                      <div className="text-muted-foreground space-y-1">
                        <p>Lunes a Viernes: 8:00 a.m. – 12:00 m. / 2:00 p.m. – 6:00 p.m.</p>
                        <p>Sábados: 8:00 a.m. – 12:00 m.</p>
                        <p className="text-sm mt-2">Domingos: Cerrado</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Button
                size="lg"
                className="w-full text-lg py-6"
                onClick={() => {
                  navigate('/agendar-cita');
                }}
              >
                Solicitar Cita
              </Button>
            </div>

            <div>
              <Card>
                <CardContent className="p-0">
                  <div className="aspect-square bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center">
                    <div className="text-center p-8">
                      <MapPin className="h-16 w-16 text-primary/40 mx-auto mb-4" />
                      <p className="text-muted-foreground text-sm">
                        {/* TODO: Integrar mapa embebido de Google Maps o similar */}
                        Mapa embebido: Calle 6 Norte # 14-26, [Ciudad]
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-foreground text-white py-12">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-3 gap-8 mb-8">
            <div>
              <div className="flex items-center gap-3 mb-4">
                <div className="h-10 w-10 rounded-full bg-primary/20 flex items-center justify-center">
                  <Dog className="h-5 w-5 text-primary" />
                </div>
                <h3 className="text-lg font-bold text-white">VetClinic Pro</h3>
              </div>
              <p className="text-gray-300 text-sm">
                Clínica veterinaria universitaria comprometida con el bienestar animal y la
                formación académica.
              </p>
            </div>

            <div>
              <h4 className="font-semibold text-white mb-4">Contacto</h4>
              <ul className="space-y-2 text-sm text-gray-300">
                <li className="flex items-center gap-2">
                  <Phone className="h-4 w-4" />
                  {/* TODO: Actualizar con teléfono real */}
                  +57 XXX XXX XXXX
                </li>
                <li className="flex items-center gap-2">
                  <Mail className="h-4 w-4" />
                  {/* TODO: Actualizar con correo real */}
                  contacto@vetclinic.com
                </li>
                <li className="flex items-center gap-2">
                  <MapPin className="h-4 w-4" />
                  {/* TODO: Actualizar con dirección real */}
                  Calle 6 Norte # 14-26, [Ciudad]
                </li>
              </ul>
            </div>

            <div>
              <h4 className="font-semibold text-white mb-4">Síguenos</h4>
              <div className="flex gap-4">
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-white hover:bg-white/10"
                  onClick={() => {
                    // TODO: Actualizar con link real de Facebook
                    window.open('https://facebook.com/vetclinic', '_blank');
                  }}
                >
                  <Facebook className="h-5 w-5" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="text-white hover:bg-white/10"
                  onClick={() => {
                    // TODO: Actualizar con link real de Instagram
                    window.open('https://instagram.com/vetclinic', '_blank');
                  }}
                >
                  <Instagram className="h-5 w-5" />
                </Button>
              </div>
            </div>
          </div>

          <div className="border-t border-gray-700 pt-8 text-center text-sm text-gray-400">
            <p>
              © {new Date().getFullYear()} VetClinic Pro. Todos los derechos reservados.
            </p>
            <div className="mt-2 space-x-4">
              <a href="#" className="hover:text-white transition-colors">
                Aviso de Privacidad
              </a>
              <span>|</span>
              <a href="#" className="hover:text-white transition-colors">
                Términos y Condiciones
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

