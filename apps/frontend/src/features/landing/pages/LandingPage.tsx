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
  GraduationCap,
  Scan,
  Monitor,
  Sparkles,
  Shield,
  Users,
  MessageSquare,
  Award,
  CheckCircle2,
  PawPrint,
} from 'lucide-react';

export default function LandingPage() {
  const navigate = useNavigate();

  const servicios = [
    {
      icon: Stethoscope,
      title: 'Consultas generales',
      description:
        'Consultas personalizadas para evaluar la salud de tu mascota. Examen físico completo, revisión de historia clínica y plan de manejo claro.',
      bullets: [
        'Evaluación clínica integral.',
        'Revisión de antecedentes y estilo de vida.',
        'Diagnóstico oportuno y recomendaciones de tratamiento.',
      ],
    },
    {
      icon: Activity,
      title: 'Exámenes físicos',
      description:
        'Chequeos periódicos para detectar problemas de salud de forma temprana y mantener a tu compañero en óptimas condiciones.',
      bullets: [
        'Revisión de ojos, oídos y nariz.',
        'Palpación abdominal y musculoesquelética.',
        'Evaluación del estado general de salud.',
      ],
    },
    {
      icon: Syringe,
      title: 'Vacunación',
      description:
        'Esquemas de vacunación diseñados según edad, especie, entorno y estilo de vida para prevenir enfermedades infecciosas.',
      bullets: [
        'Programas de vacunación personalizados.',
        'Asesoría sobre refuerzos y calendarios.',
        'Prevención de enfermedades infecciosas clave.',
      ],
    },
    {
      icon: Microscope,
      title: 'Análisis de laboratorio',
      description:
        'Apoyo diagnóstico mediante pruebas completas que permiten detectar enfermedades agudas y crónicas.',
      bullets: [
        'Pruebas de sangre y orina.',
        'Análisis de heces y otros fluidos.',
        'Diagnóstico de enfermedades infecciosas y crónicas.',
      ],
    },
    {
      icon: Scan,
      title: 'Radiología y ecografía',
      description:
        'Imágenes de alta calidad para evaluar estructuras óseas, órganos internos y apoyar el diagnóstico pre y postoperatorio.',
      bullets: [
        'Radiografías digitales.',
        'Ecografía para órganos internos.',
        'Valoración pre y postquirúrgica.',
      ],
    },
    {
      icon: Scissors,
      title: 'Cirugía general',
      description:
        'Procedimientos menores y mayores realizados bajo protocolos seguros y con equipos de última generación.',
      bullets: [
        'Esterilizaciones y cirugía de tejidos blandos.',
        'Tratamiento quirúrgico de enfermedades internas.',
        'Manejo avanzado de heridas y cirugía oncológica.',
      ],
    },
    {
      icon: Heart,
      title: 'Cuidado intensivo',
      description:
        'Atención especializada para pacientes críticos que requieren monitoreo constante y soporte avanzado.',
      bullets: [
        'Monitoreo continuo de signos vitales.',
        'Administración de medicamentos y fluidos.',
        'Apoyo respiratorio y cardíaco.',
      ],
    },
  ];

  const equipos = [
    {
      title: 'Monitor de signos vitales',
      description: 'Para monitorear a los animales durante la intervención quirúrgica.',
    },
    {
      title: 'Mesa quirúrgica graduable',
      description: 'Capacidad para la atención de perros y gatos de cualquier tamaño.',
    },
    {
      title: 'Rayos X digitales',
      description: 'Radiografías digitales de alta definición para diagnósticos precisos.',
    },
    {
      title: 'Succionador',
      description: 'Equipo para el manejo de derrames y hemorragias durante procedimientos.',
    },
    {
      title: 'Máquina de anestesia inhalada',
      description: 'Anestesia segura y controlada durante cirugías.',
    },
    {
      title: 'Ventilador veterinario',
      description: 'Ventilación mecánica especializada para perros y gatos.',
    },
    {
      title: 'Ecógrafo Doppler',
      description:
        'Permite medir la circulación sanguínea y realizar ecocardiogramas, estudios torácicos, articulares, musculares y vasculares.',
    },
    {
      title: 'Electrobisturí',
      description:
        'Corte y cauterización en un solo equipo: esterilizaciones, resección de órganos y cirugía de superficies.',
    },
  ];

  const valores = [
    {
      icon: Heart,
      title: 'Compasión',
      description:
        'Entendemos el valor emocional de los animales de compañía y brindamos una atención respetuosa y empática.',
    },
    {
      icon: Award,
      title: 'Excelencia',
      description:
        'Mantenemos altos estándares de calidad en nuestros servicios, procedimientos y atención médica.',
    },
    {
      icon: Sparkles,
      title: 'Innovación',
      description:
        'Buscamos constantemente nuevas tecnologías y técnicas vanguardistas para mejorar el diagnóstico y tratamiento.',
    },
    {
      icon: Shield,
      title: 'Integridad',
      description:
        'Actuamos con honestidad, transparencia y ética en todas nuestras decisiones y relaciones.',
    },
    {
      icon: CheckCircle2,
      title: 'Responsabilidad',
      description:
        'Promovemos prácticas sostenibles y un impacto positivo en la comunidad y el entorno.',
    },
    {
      icon: MessageSquare,
      title: 'Comunicación',
      description:
        'Fomentamos una comunicación clara y cercana con nuestros clientes para tomar decisiones informadas.',
    },
    {
      icon: Users,
      title: 'Respeto',
      description:
        'Valoramos la diversidad y las necesidades particulares de cada animal y su familia.',
    },
    {
      icon: GraduationCap,
      title: 'Colaboración',
      description:
        'Trabajamos en equipo para asegurar la mejor atención médica y apoyar la formación de futuros profesionales.',
    },
  ];

  const objetivosEstrategicos = [
    'Expandir nuestros servicios para incluir especialidades médicas y quirúrgicas.',
    'Implementar tecnologías innovadoras para mejorar la eficiencia y la calidad de la atención.',
    'Establecer alianzas con organizaciones de bienestar animal para promover la educación y el cuidado responsable.',
    'Desarrollar programas de educación y prevención para dueños de mascotas.',
    'Fortalecer nuestra presencia en línea y redes sociales para mejorar la comunicación con nuestros clientes.',
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
              <h1 className="text-xl font-bold tracking-tight">
                Clínica Veterinaria Universitaria Humboldt
              </h1>
              <p className="text-xs text-muted-foreground">VNZ</p>
            </div>
          </div>

          <Button onClick={() => navigate('/login')} size="lg" className="font-semibold">
            Iniciar Sesión
          </Button>
        </div>
      </header>

      {/* HERO */}
      <section className="container mx-auto px-4 py-20 lg:py-28 grid lg:grid-cols-2 gap-12 items-center">
        <div className="space-y-6">
          <h2 className="text-5xl font-extrabold leading-tight tracking-tight">
            Cuidado veterinario <span className="text-primary">de alta calidad</span>
            <br />
            para quienes más amas
          </h2>
          <p className="text-xl text-muted-foreground max-w-lg">
            Somos una clínica veterinaria universitaria que combina experiencia clínica,
            tecnología moderna y vocación de servicio para garantizar el bienestar de tus
            animales de compañía.
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

      {/* BENEFICIOS */}
      <section className="bg-primary/5 py-10 border-y">
        <div className="container mx-auto px-4 grid md:grid-cols-3 gap-6 text-center">
          <div>
            <PawPrint className="h-10 w-10 text-primary mx-auto mb-2" />
            <h4 className="font-bold text-lg">Atención integral</h4>
            <p className="text-sm text-muted-foreground">
              Medicina preventiva, diagnóstico y tratamiento en un solo lugar.
            </p>
          </div>
          <div>
            <Monitor className="h-10 w-10 text-primary mx-auto mb-2" />
            <h4 className="font-bold text-lg">Equipos de última tecnología</h4>
            <p className="text-sm text-muted-foreground">
              Imagenología, monitoreo y laboratorio clínico avanzados.
            </p>
          </div>
          <div>
            <Users className="h-10 w-10 text-primary mx-auto mb-2" />
            <h4 className="font-bold text-lg">Clínica universitaria</h4>
            <p className="text-sm text-muted-foreground">
              Formación de futuros veterinarios bajo supervisión de expertos.
            </p>
          </div>
        </div>
      </section>

      {/* QUIÉNES SOMOS */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-4 max-w-4xl">
          <h2 className="text-4xl font-bold text-center mb-6">¿Quiénes somos?</h2>
          <p className="text-lg text-center text-muted-foreground">
            Somos una clínica veterinaria universitaria comprometida con brindar atención
            médica integral y personalizada a los animales, priorizando su bienestar y calidad
            de vida. Nuestro equipo de expertos veterinarios y personal capacitado se esfuerza
            por proporcionar servicios de alta calidad, innovación y compasión.
          </p>
        </div>
      </section>

      {/* SERVICIOS MÉDICOS */}
      <section id="servicios" className="py-20 bg-white">
        <div className="container mx-auto px-4">
          <div className="text-center mb-14">
            <h2 className="text-4xl font-bold mb-2">Servicios médicos</h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Todo lo que tu mascota necesita, desde prevención hasta cuidado intensivo.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-8">
            {servicios.map((servicio) => {
              const Icon = servicio.icon;
              return (
                <Card
                  key={servicio.title}
                  className="border hover:shadow-xl hover:border-primary/50 transition-all h-full"
                >
                  <CardContent className="p-6 space-y-4">
                    <div className="flex items-center gap-3">
                      <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center">
                        <Icon className="h-6 w-6 text-primary" />
                      </div>
                      <h3 className="font-semibold text-xl">{servicio.title}</h3>
                    </div>
                    <p className="text-sm text-muted-foreground">{servicio.description}</p>
                    {servicio.bullets && (
                      <ul className="text-sm text-muted-foreground list-disc list-inside space-y-1">
                        {servicio.bullets.map((item) => (
                          <li key={item}>{item}</li>
                        ))}
                      </ul>
                    )}
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* EQUIPOS DE ÚLTIMA TECNOLOGÍA */}
      <section className="bg-primary/5 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold mb-2">Equipos de última tecnología</h2>
            <p className="text-lg text-muted-foreground max-w-3xl mx-auto">
              Contamos con una infraestructura moderna que respalda cada procedimiento y
              garantiza diagnósticos confiables.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {equipos.map((equipo) => (
              <Card key={equipo.title} className="overflow-hidden">
                <CardContent className="p-6 space-y-2">
                  <h3 className="font-semibold text-lg">{equipo.title}</h3>
                  <p className="text-sm text-muted-foreground">{equipo.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA INTERMEDIO */}
      <section className="py-16 bg-gradient-to-r from-primary to-blue-600 text-white text-center">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold mb-4">
            Agenda la salud de tu mascota hoy mismo
          </h2>
          <p className="mb-6 text-lg opacity-90">
            Nuestro equipo está listo para brindarte atención profesional, cercana y humana.
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
          <h2 className="text-4xl font-bold text-center mb-12">Nuestra filosofía y valores</h2>

          <p className="text-lg text-center text-muted-foreground mb-10">
            Creemos que los animales son miembros valiosos de la familia y merecen recibir
            atención médica excepcional. Nos enfocamos en fortalecer el vínculo entre humanos y
            animales a través de la educación, el cuidado y el amor.
          </p>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {valores.map((valor) => {
              const Icon = valor.icon;
              return (
                <Card
                  key={valor.title}
                  className="text-center hover:shadow-lg transition-all h-full"
                >
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

      {/* OBJETIVOS ESTRATÉGICOS */}
      <section className="py-20 bg-primary/5">
        <div className="container mx-auto px-4 max-w-4xl">
          <h2 className="text-4xl font-bold text-center mb-8">Objetivos estratégicos</h2>
          <ul className="space-y-4 text-muted-foreground">
            {objetivosEstrategicos.map((objetivo) => (
              <li key={objetivo} className="flex gap-3 items-start">
                <CheckCircle2 className="h-5 w-5 text-primary mt-1 flex-shrink-0" />
                <span className="text-base">{objetivo}</span>
              </li>
            ))}
          </ul>
        </div>
      </section>

      {/* CONTACTO */}
      <section id="contacto" className="py-20 bg-white">
        <div className="container mx-auto px-4 grid lg:grid-cols-2 gap-12">
          <div className="space-y-8">
            <h2 className="text-4xl font-bold">Contacto y ubicación</h2>
            <p className="text-lg text-muted-foreground">
              Estamos aquí para cuidar de tu mascota y acompañarte en cada etapa de su vida.
            </p>

            <Card>
              <CardContent className="p-6 flex gap-4">
                <MapPin className="h-6 w-6 text-primary" />
                <div>
                  <h3 className="font-semibold">Dirección</h3>
                  <p className="text-muted-foreground">
                    Calle 6 Norte # 14-26, [Ciudad]
                  </p>
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
            <h3 className="font-bold text-lg mb-3">Clínica Veterinaria Universitaria Humboldt</h3>
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
              <a
                href="https://www.instagram.com/clinicaveterinariahumboldt/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-gray-300 hover:text-white transition-colors"
                aria-label="Facebook"
              >
                <svg
                  className="h-5 w-5"
                  fill="currentColor"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"
                    clipRule="evenodd"
                  />
                </svg>
              </a>
              <a
                href="https://www.instagram.com/clinicaveterinariahumboldt/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-gray-300 hover:text-white transition-colors"
                aria-label="Instagram"
              >
                <svg
                  className="h-5 w-5"
                  fill="currentColor"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M12.315 2c2.43 0 2.784.013 3.808.06 1.064.049 1.791.218 2.427.465a4.902 4.902 0 011.772 1.153 4.902 4.902 0 011.153 1.772c.247.636.416 1.363.465 2.427.048 1.067.06 1.407.06 4.123v.08c0 2.643-.012 2.987-.06 4.043-.049 1.064-.218 1.791-.465 2.427a4.902 4.902 0 01-1.153 1.772 4.902 4.902 0 01-1.772 1.153c-.636.247-1.363.416-2.427.465-1.067.048-1.407.06-4.123.06h-.08c-2.643 0-2.987-.012-4.043-.06-1.064-.049-1.791-.218-2.427-.465a4.902 4.902 0 01-1.772-1.153 4.902 4.902 0 01-1.153-1.772c-.247-.636-.416-1.363-.465-2.427-.047-1.024-.06-1.379-.06-3.808v-.63c0-2.43.013-2.784.06-3.808.049-1.064.218-1.791.465-2.427a4.902 4.902 0 011.153-1.772A4.902 4.902 0 015.45 2.525c.636-.247 1.363-.416 2.427-.465C8.901 2.013 9.256 2 11.685 2h.63zm-.081 1.802h-.468c-2.456 0-2.784.011-3.807.058-.975.045-1.504.207-1.857.344-.467.182-.8.398-1.15.748-.35.35-.566.683-.748 1.15-.137.353-.3.882-.344 1.857-.047 1.023-.058 1.351-.058 3.807v.468c0 2.456.011 2.784.058 3.807.045.975.207 1.504.344 1.857.182.466.399.8.748 1.15.35.35.683.566 1.15.748.353.137.882.3 1.857.344 1.054.048 1.37.058 4.041.058h.08c2.597 0 2.917-.01 3.96-.058.976-.045 1.505-.207 1.858-.344.466-.182.8-.398 1.15-.748.35-.35.566-.683.748-1.15.137-.353.3-.882.344-1.857.048-1.055.058-1.37.058-4.041v-.08c0-2.597-.01-2.917-.058-3.96-.045-.976-.207-1.505-.344-1.858a3.097 3.097 0 00-.748-1.15 3.098 3.098 0 00-1.15-.748c-.353-.137-.882-.3-1.857-.344-1.023-.047-1.351-.058-3.807-.058zM12 6.865a5.135 5.135 0 110 10.27 5.135 5.135 0 010-10.27zm0 1.802a3.333 3.333 0 100 6.666 3.333 3.333 0 000-6.666zm5.338-3.205a1.2 1.2 0 110 2.4 1.2 1.2 0 010-2.4z"
                    clipRule="evenodd"
                  />
                </svg>
              </a>
            </div>
          </div>
        </div>

        <p className="text-center text-gray-400 text-sm mt-6">
          © {new Date().getFullYear()} Clínica Veterinaria Universitaria Humboldt — Todos los
          derechos reservados.
        </p>
      </footer>
    </div>
  );
}
