import { useNavigate } from 'react-router-dom';
import { useState, useEffect, useRef, useCallback } from 'react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Input } from '@shared/components/ui/input';
import { Progress } from '@shared/components/ui/progress';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@shared/components/ui/sheet';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@shared/components/ui/accordion';
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselPrevious,
  CarouselNext,
} from '@shared/components/ui/carousel';
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
  ArrowRight,
  Star,
  Clock,
  Calendar,
  Menu,
  X,
  ChevronUp,
  Mail,
  Send,
  Quote,
  HelpCircle,
} from 'lucide-react';

// DATA -----------------------------------------------------------------------

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

const testimonios = [
  {
    nombre: 'María García',
    mascota: 'Luna (Labrador)',
    texto: 'Excelente atención para mi perrita Luna. El equipo médico es muy profesional y cariñoso con los animales. Totalmente recomendados.',
    rating: 5,
    imagen: '/placeholder-avatar.jpg',
  },
  {
    nombre: 'Carlos Rodríguez',
    mascota: 'Michi (Gato)',
    texto: 'Mi gato Michi fue atendido de emergencia y el servicio fue impecable. Muy agradecido con todo el personal de la clínica.',
    rating: 5,
    imagen: '/placeholder-avatar.jpg',
  },
  {
    nombre: 'Ana Martínez',
    mascota: 'Rocky (Bulldog)',
    texto: 'La mejor clínica veterinaria de la ciudad. Equipos modernos, precios justos y sobre todo, mucho amor por los animales.',
    rating: 5,
    imagen: '/placeholder-avatar.jpg',
  },
  {
    nombre: 'Pedro Sánchez',
    mascota: 'Toby (Golden Retriever)',
    texto: 'Llevamos años trayendo a Toby aquí. La confianza y profesionalismo que brindan no tiene precio. Gracias por cuidar de nuestra familia.',
    rating: 5,
    imagen: '/placeholder-avatar.jpg',
  },
];

const preguntasFrecuentes = [
  {
    pregunta: '¿Cuál es el horario de atención de la clínica?',
    respuesta: 'Nuestro horario de atención regular es de lunes a viernes de 8:00 AM a 6:00 PM, y sábados de 8:00 AM a 2:00 PM. Para emergencias, contamos con servicio 24/7.',
  },
  {
    pregunta: '¿Necesito agendar una cita previa?',
    respuesta: 'Recomendamos agendar cita previa para consultas regulares a través de nuestra plataforma en línea o por teléfono. Sin embargo, atendemos emergencias sin cita previa.',
  },
  {
    pregunta: '¿Qué métodos de pago aceptan?',
    respuesta: 'Aceptamos efectivo, tarjetas de débito y crédito, y transferencias bancarias. También ofrecemos planes de financiamiento para procedimientos mayores.',
  },
  {
    pregunta: '¿Atienden animales exóticos?',
    respuesta: 'Actualmente nos especializamos en perros y gatos. Para animales exóticos, podemos referirle a especialistas de nuestra red de contactos.',
  },
  {
    pregunta: '¿Ofrecen servicios de peluquería y estética?',
    respuesta: 'Sí, contamos con servicio de baño, corte de pelo, corte de uñas y limpieza de oídos. Estos servicios se pueden agendar junto con la consulta veterinaria.',
  },
  {
    pregunta: '¿Tienen servicio de hospitalización?',
    respuesta: 'Sí, contamos con área de hospitalización con monitoreo 24 horas para pacientes que requieren cuidados intensivos o recuperación postquirúrgica.',
  },
];

const estadisticas = [
  { numero: 15, sufijo: '+', label: 'Años de experiencia', icon: Award },
  { numero: 5000, sufijo: '+', label: 'Pacientes atendidos', icon: PawPrint },
  { numero: 98, sufijo: '%', label: 'Clientes satisfechos', icon: Heart },
  { numero: 1500, sufijo: '+', label: 'Cirugías exitosas', icon: Stethoscope },
];

const equipoMedico = [
  {
    nombre: 'Dr. Juan Pérez',
    cargo: 'Director Médico',
    especialidad: 'Cirugía General',
    imagen: '/placeholder-doctor.jpg',
  },
  {
    nombre: 'Dra. Laura Gómez',
    cargo: 'Veterinaria Senior',
    especialidad: 'Medicina Interna',
    imagen: '/placeholder-doctor.jpg',
  },
  {
    nombre: 'Dr. Miguel Torres',
    cargo: 'Especialista',
    especialidad: 'Imagenología',
    imagen: '/placeholder-doctor.jpg',
  },
  {
    nombre: 'Dra. Carolina Ruiz',
    cargo: 'Veterinaria',
    especialidad: 'Dermatología',
    imagen: '/placeholder-doctor.jpg',
  },
];

// Custom hook for scroll reveal animations
function useScrollReveal() {
  const ref = useRef<HTMLDivElement>(null);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
    );

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => observer.disconnect();
  }, []);

  return { ref, isVisible };
}

// Custom hook for animated counter
function useAnimatedCounter(end: number, duration: number = 2000, isVisible: boolean) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (!isVisible) return;

    let startTime: number;
    let animationFrame: number;

    const animate = (timestamp: number) => {
      if (!startTime) startTime = timestamp;
      const progress = Math.min((timestamp - startTime) / duration, 1);
      
      setCount(Math.floor(progress * end));

      if (progress < 1) {
        animationFrame = requestAnimationFrame(animate);
      }
    };

    animationFrame = requestAnimationFrame(animate);

    return () => cancelAnimationFrame(animationFrame);
  }, [end, duration, isVisible]);

  return count;
}

// Scroll reveal wrapper component
function ScrollReveal({ 
  children, 
  className = '',
  delay = 0 
}: { 
  children: React.ReactNode; 
  className?: string;
  delay?: number;
}) {
  const { ref, isVisible } = useScrollReveal();

  return (
    <div
      ref={ref}
      className={`transition-all duration-700 ${className}`}
      style={{
        transitionDelay: `${delay}ms`,
        opacity: isVisible ? 1 : 0,
        transform: isVisible ? 'translateY(0)' : 'translateY(30px)',
      }}
    >
      {children}
    </div>
  );
}

// Animated counter component
function AnimatedStat({ 
  numero, 
  sufijo, 
  label, 
  icon: Icon,
  isVisible 
}: { 
  numero: number; 
  sufijo: string; 
  label: string; 
  icon: React.ElementType;
  isVisible: boolean;
}) {
  const count = useAnimatedCounter(numero, 2000, isVisible);

  return (
    <div className="text-center group">
      <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20 transition-transform duration-300 group-hover:scale-110">
        <Icon className="h-8 w-8 text-white" />
      </div>
      <div className="text-4xl font-bold text-white md:text-5xl">
        {count.toLocaleString()}{sufijo}
      </div>
      <div className="mt-2 text-sm text-white/80 md:text-base">{label}</div>
    </div>
  );
}

// COMPONENTE -----------------------------------------------------------------

export default function LandingPage() {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [scrollProgress, setScrollProgress] = useState(0);
  const [showBackToTop, setShowBackToTop] = useState(false);
  const [statsVisible, setStatsVisible] = useState(false);
  const statsRef = useRef<HTMLDivElement>(null);

  // Scroll progress and back to top visibility
  useEffect(() => {
    const handleScroll = () => {
      const totalHeight = document.documentElement.scrollHeight - window.innerHeight;
      const progress = (window.scrollY / totalHeight) * 100;
      setScrollProgress(progress);
      setShowBackToTop(window.scrollY > 500);
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Stats visibility observer
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setStatsVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.3 }
    );

    if (statsRef.current) {
      observer.observe(statsRef.current);
    }

    return () => observer.disconnect();
  }, []);

  const scrollToSection = useCallback((id: string) => {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    setMobileMenuOpen(false);
  }, []);

  const scrollToTop = useCallback(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-b from-white via-primary/5 to-white scroll-smooth text-foreground">
      {/* SCROLL PROGRESS INDICATOR */}
      <div className="fixed top-0 left-0 right-0 z-[60] h-1 bg-gray-200">
        <Progress 
          value={scrollProgress} 
          className="h-1 rounded-none bg-transparent"
        />
      </div>

      {/* HEADER */}
      <header className="sticky top-1 z-50 w-full border-b bg-white/90 backdrop-blur-xl shadow-sm transition-all duration-300">
        <div className="container mx-auto flex items-center justify-between gap-4 px-4 py-3 lg:py-4">
          <button
            type="button"
            className="flex items-center gap-3 group cursor-pointer bg-transparent border-none p-0"
            onClick={scrollToTop}
            aria-label="Ir al inicio"
          >
            <div className="flex h-10 w-10 md:h-12 md:w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary/60 shadow-lg transition-transform duration-300 group-hover:scale-110">
              <Dog className="h-5 w-5 md:h-6 md:w-6 text-white" />
            </div>
            <div className="hidden sm:flex flex-col text-left">
              <span className="bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-xs font-semibold uppercase tracking-[0.12em] text-transparent">
                Clínica universitaria
              </span>
              <h1 className="text-sm sm:text-base font-bold leading-tight tracking-tight lg:text-lg">
                Clínica Veterinaria Universitaria Humboldt
              </h1>
            </div>
          </button>

          {/* Desktop Navigation */}
          <nav className="hidden items-center gap-6 text-sm font-medium text-muted-foreground lg:flex">
            <button
              type="button"
              onClick={() => scrollToSection('servicios')}
              className="transition-colors hover:text-primary focus:text-primary focus:outline-none"
            >
              Servicios
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('sobre-nosotros')}
              className="transition-colors hover:text-primary focus:text-primary focus:outline-none"
            >
              Sobre nosotros
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('equipo')}
              className="transition-colors hover:text-primary focus:text-primary focus:outline-none"
            >
              Equipo
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('testimonios')}
              className="transition-colors hover:text-primary focus:text-primary focus:outline-none"
            >
              Testimonios
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('faq')}
              className="transition-colors hover:text-primary focus:text-primary focus:outline-none"
            >
              FAQ
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('contacto')}
              className="transition-colors hover:text-primary focus:text-primary focus:outline-none"
            >
              Contacto
            </button>
          </nav>

          <div className="flex items-center gap-2 md:gap-3">
            <Button
              variant="outline"
              size="sm"
              className="hidden border-primary/40 text-xs font-semibold text-primary shadow-sm hover:bg-primary/5 md:inline-flex"
              onClick={() => scrollToSection('contacto')}
            >
              <Phone className="mr-1.5 h-3.5 w-3.5" />
              Emergencias 24/7
            </Button>
            <Button
              onClick={() => navigate('/login')}
              size="sm"
              className="font-semibold shadow-md hover:shadow-lg hover:scale-105 transition-all duration-300"
            >
              Iniciar sesión
            </Button>

            {/* Mobile Menu Button */}
            <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
              <SheetTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="lg:hidden"
                  aria-label="Abrir menú"
                >
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-[300px] sm:w-[350px]">
                <SheetHeader>
                  <SheetTitle className="flex items-center gap-2">
                    <Dog className="h-5 w-5 text-primary" />
                    Menú
                  </SheetTitle>
                </SheetHeader>
                <nav className="mt-8 flex flex-col gap-4">
                  <button
                    type="button"
                    onClick={() => scrollToSection('servicios')}
                    className="flex items-center gap-3 rounded-lg p-3 text-left font-medium transition-colors hover:bg-primary/10 hover:text-primary"
                  >
                    <Stethoscope className="h-5 w-5" />
                    Servicios
                  </button>
                  <button
                    type="button"
                    onClick={() => scrollToSection('sobre-nosotros')}
                    className="flex items-center gap-3 rounded-lg p-3 text-left font-medium transition-colors hover:bg-primary/10 hover:text-primary"
                  >
                    <Users className="h-5 w-5" />
                    Sobre nosotros
                  </button>
                  <button
                    type="button"
                    onClick={() => scrollToSection('equipo')}
                    className="flex items-center gap-3 rounded-lg p-3 text-left font-medium transition-colors hover:bg-primary/10 hover:text-primary"
                  >
                    <GraduationCap className="h-5 w-5" />
                    Equipo médico
                  </button>
                  <button
                    type="button"
                    onClick={() => scrollToSection('testimonios')}
                    className="flex items-center gap-3 rounded-lg p-3 text-left font-medium transition-colors hover:bg-primary/10 hover:text-primary"
                  >
                    <Star className="h-5 w-5" />
                    Testimonios
                  </button>
                  <button
                    type="button"
                    onClick={() => scrollToSection('faq')}
                    className="flex items-center gap-3 rounded-lg p-3 text-left font-medium transition-colors hover:bg-primary/10 hover:text-primary"
                  >
                    <HelpCircle className="h-5 w-5" />
                    Preguntas frecuentes
                  </button>
                  <button
                    type="button"
                    onClick={() => scrollToSection('contacto')}
                    className="flex items-center gap-3 rounded-lg p-3 text-left font-medium transition-colors hover:bg-primary/10 hover:text-primary"
                  >
                    <Phone className="h-5 w-5" />
                    Contacto
                  </button>
                  <div className="mt-4 border-t pt-4">
                    <Button
                      className="w-full"
                      onClick={() => {
                        setMobileMenuOpen(false);
                        navigate('/agendar-cita');
                      }}
                    >
                      <Calendar className="mr-2 h-4 w-4" />
                      Agendar cita
                    </Button>
                  </div>
                </nav>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </header>

      <main>
        {/* HERO */}
        <section className="relative overflow-hidden">
          <div className="absolute inset-0 -z-10 overflow-hidden">
            <div className="absolute left-10 top-20 h-72 w-72 animate-pulse rounded-full bg-primary/10 blur-3xl" />
            <div className="absolute bottom-20 right-10 h-96 w-96 animate-pulse rounded-full bg-secondary/10 blur-3xl" />
          </div>

          <div className="container mx-auto grid items-center gap-12 px-4 py-16 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,0.9fr)] lg:py-24">
            {/* Hero content */}
            <div className="relative z-10 space-y-7 lg:space-y-8">
              <div className="inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/10 px-4 py-1.5 text-xs font-semibold text-primary shadow-sm">
                <Star className="h-4 w-4 fill-primary text-primary" />
                <span>Clínica veterinaria universitaria certificada</span>
              </div>

              <h2 className="text-balance text-4xl font-extrabold leading-tight tracking-tight sm:text-5xl lg:text-6xl">
                Cuidado veterinario{' '}
                <span className="relative inline-block">
                  <span className="relative z-10 text-primary">de alta calidad</span>
                  <span className="absolute -z-0 bottom-1 left-0 h-3 w-full bg-primary/20" />
                </span>
                <br />
                para quienes más amas
              </h2>

              <p className="max-w-xl text-pretty text-base text-muted-foreground sm:text-lg lg:text-xl">
                Somos una clínica veterinaria universitaria que combina experiencia clínica,
                tecnología moderna y vocación de servicio para garantizar el bienestar de tus
                animales de compañía.
              </p>

              {/* Quick facts */}
              <div className="flex flex-wrap items-center gap-x-6 gap-y-3 text-sm text-muted-foreground">
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-primary" />
                  <span>Horarios flexibles</span>
                </div>
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-primary" />
                  <span>Citas en línea</span>
                </div>
                <div className="flex items-center gap-2">
                  <Heart className="h-4 w-4 text-primary" />
                  <span>Atención 24/7 en emergencias</span>
                </div>
              </div>

              {/* CTAs */}
              <div className="flex flex-col gap-4 pt-2 sm:flex-row sm:items-center">
                <Button
                  size="lg"
                  className="group flex items-center justify-center gap-2 px-8 py-6 text-lg font-semibold shadow-lg transition-all duration-300 hover:scale-105 hover:shadow-xl"
                  onClick={() => navigate('/agendar-cita')}
                >
                  Solicitar cita
                  <ArrowRight className="h-5 w-5 transition-transform group-hover:translate-x-1" />
                </Button>

                <Button
                  size="lg"
                  variant="outline"
                  className="flex items-center justify-center gap-2 px-8 py-6 text-lg border-2 hover:bg-primary/5 transition-all duration-300"
                  onClick={() => scrollToSection('servicios')}
                >
                  Ver servicios
                </Button>
              </div>

              {/* Stats bar */}
              <div className="mt-4 grid gap-4 rounded-2xl border border-primary/10 bg-white/70 p-4 text-sm shadow-sm backdrop-blur-sm sm:grid-cols-3">
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10">
                    <Award className="h-4 w-4 text-primary" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Formación universitaria</p>
                    <p className="text-sm font-semibold">Docentes especialistas</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10">
                    <Monitor className="h-4 w-4 text-primary" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Tecnología avanzada</p>
                    <p className="text-sm font-semibold">Diagnóstico preciso</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10">
                    <Users className="h-4 w-4 text-primary" />
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">+1000 pacientes</p>
                    <p className="text-sm font-semibold">Confían en nosotros</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Hero illustration */}
            <div className="relative z-10">
              <div className="group relative aspect-square overflow-hidden rounded-3xl shadow-2xl">
                <div className="absolute inset-0 animate-gradient-xy bg-gradient-to-br from-primary via-primary/80 to-secondary/60" />

                {/* Decor circles */}
                <div className="absolute inset-0 opacity-30">
                  <div className="absolute left-10 top-10 h-32 w-32 rounded-full border-4 border-white/30" />
                  <div className="absolute bottom-10 right-10 h-24 w-24 rounded-full border-4 border-white/30" />
                </div>

                {/* Center dog icon */}
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="relative">
                    <div className="absolute inset-0 rounded-full bg-white/20 blur-2xl transition-all duration-500 group-hover:blur-3xl" />
                    <Dog className="relative z-10 h-44 w-44 text-white transition-transform duration-500 group-hover:scale-110 lg:h-52 lg:w-52" />
                  </div>
                </div>

                {/* Floating badges */}
                <div className="absolute right-6 top-6 rounded-full bg-white/90 px-3 py-1.5 shadow-lg backdrop-blur-sm">
                  <div className="flex items-center gap-2 text-xs font-semibold text-foreground">
                    <Award className="h-4 w-4 text-primary" />
                    <span>Clínica certificada</span>
                  </div>
                </div>

                <div className="absolute bottom-6 left-6 rounded-2xl bg-white/90 px-4 py-2 shadow-lg backdrop-blur-sm">
                  <div className="flex items-center gap-2 text-xs font-semibold text-foreground">
                    <Users className="h-4 w-4 text-primary" />
                    <span>Atención humana y cercana</span>
                  </div>
                </div>

                <div className="absolute inset-x-10 bottom-10 rounded-2xl border border-white/40 bg-black/20 px-4 py-3 text-xs text-white backdrop-blur-md lg:text-sm">
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2">
                      <PawPrint className="h-4 w-4" />
                      <span>Medicina preventiva, diagnóstica y quirúrgica</span>
                    </div>
                    <span className="hidden rounded-full bg-white/20 px-3 py-1 text-[11px] font-semibold uppercase tracking-wide lg:inline">
                      Humboldt
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* BENEFICIOS */}
        <section className="relative border-y border-primary/10 bg-gradient-to-b from-primary/5 via-white to-primary/5 py-14 lg:py-16">
          <div className="container mx-auto px-4">
            <div className="grid gap-6 md:grid-cols-3">
              <Card className="border-2 border-primary/15 bg-white/80 backdrop-blur-sm transition-all duration-300 hover:-translate-y-2 hover:border-primary/40 hover:shadow-xl">
                <CardContent className="p-6 text-center">
                  <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-primary/70 shadow-lg">
                    <PawPrint className="h-8 w-8 text-white" />
                  </div>
                  <h3 className="mb-2 text-xl font-bold">Atención integral</h3>
                  <p className="text-sm leading-relaxed text-muted-foreground">
                    Medicina preventiva, diagnóstico y tratamiento en un solo lugar.
                  </p>
                </CardContent>
              </Card>

              <Card className="border-2 border-primary/15 bg-white/80 backdrop-blur-sm transition-all duration-300 hover:-translate-y-2 hover:border-primary/40 hover:shadow-xl">
                <CardContent className="p-6 text-center">
                  <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-secondary to-secondary/70 shadow-lg">
                    <Monitor className="h-8 w-8 text-white" />
                  </div>
                  <h3 className="mb-2 text-xl font-bold">Tecnología de punta</h3>
                  <p className="text-sm leading-relaxed text-muted-foreground">
                    Imagenología, monitoreo y laboratorio clínico avanzados.
                  </p>
                </CardContent>
              </Card>

              <Card className="border-2 border-primary/15 bg-white/80 backdrop-blur-sm transition-all duration-300 hover:-translate-y-2 hover:border-primary/40 hover:shadow-xl">
                <CardContent className="p-6 text-center">
                  <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-secondary shadow-lg">
                    <Users className="h-8 w-8 text-white" />
                  </div>
                  <h3 className="mb-2 text-xl font-bold">Clínica universitaria</h3>
                  <p className="text-sm leading-relaxed text-muted-foreground">
                    Formación de futuros veterinarios bajo supervisión de expertos.
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* QUIÉNES SOMOS */}
        <section
          id="sobre-nosotros"
          className="relative overflow-hidden bg-white py-20 lg:py-24"
        >
          <div className="pointer-events-none absolute inset-0 bg-grid-pattern opacity-5" />
          <div className="container relative z-10 mx-auto max-w-5xl px-4">
            <div className="mb-10 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Sobre nosotros
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                ¿Quiénes somos?
              </h2>
            </div>

            <Card className="border-2 border-primary/10 bg-gradient-to-br from-white to-primary/5 shadow-xl">
              <CardContent className="space-y-5 p-7 lg:p-10">
                <p className="text-center text-base leading-relaxed text-muted-foreground sm:text-lg lg:text-xl">
                  Somos una clínica veterinaria universitaria comprometida con brindar atención
                  médica integral y personalizada a los animales, priorizando su bienestar y calidad
                  de vida. Nuestro equipo de expertos veterinarios y personal capacitado se esfuerza
                  por proporcionar servicios de alta calidad, innovación y compasión.
                </p>
                <p className="text-center text-sm leading-relaxed text-muted-foreground sm:text-base">
                  Como parte de la Universidad, también somos un espacio de formación donde los
                  futuros médicos veterinarios adquieren experiencia práctica bajo la guía de
                  profesionales con amplia trayectoria.
                </p>
              </CardContent>
            </Card>
          </div>
        </section>

        {/* SERVICIOS MÉDICOS */}
        <section
          id="servicios"
          className="relative bg-gradient-to-b from-white to-primary/5 py-20 lg:py-24"
        >
          <div className="container mx-auto px-4">
            <div className="mb-14 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Nuestros servicios
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Servicios médicos
              </h2>
              <p className="mx-auto mt-3 max-w-2xl text-base text-muted-foreground sm:text-lg">
                Todo lo que tu mascota necesita, desde prevención hasta cuidado intensivo.
              </p>
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:gap-8">
              {servicios.map((servicio, index) => {
                const Icon = servicio.icon;
                return (
                  <Card
                    key={servicio.title}
                    className="group relative h-full overflow-hidden border-2 border-primary/10 bg-white/90 backdrop-blur-sm transition-all duration-300 hover:-translate-y-1 hover:border-primary/40 hover:shadow-2xl"
                    style={{ animationDelay: `${index * 80}ms` }}
                  >
                    <CardContent className="relative space-y-5 p-6 lg:p-7">
                      <div className="pointer-events-none absolute inset-0 -z-0 bg-gradient-to-br from-primary/0 via-primary/0 to-primary/0 transition-all duration-500 group-hover:from-primary/5 group-hover:via-primary/10 group-hover:to-primary/5" />

                      <div className="relative z-10 flex items-start gap-4">
                        <div className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-primary/70 shadow-lg transition-all duration-300 group-hover:scale-110 group-hover:rotate-3">
                          <Icon className="h-7 w-7 text-white" />
                        </div>
                        <div className="flex-1">
                          <h3 className="mb-1.5 text-lg font-bold lg:text-xl group-hover:text-primary transition-colors">
                            {servicio.title}
                          </h3>
                          <p className="text-sm leading-relaxed text-muted-foreground lg:text-[15px]">
                            {servicio.description}
                          </p>
                        </div>
                      </div>

                      {servicio.bullets && (
                        <ul className="relative z-10 mt-2 space-y-2.5 border-t border-primary/10 pt-3">
                          {servicio.bullets.map((item) => (
                            <li
                              key={item}
                              className="flex items-start gap-2 text-sm text-muted-foreground"
                            >
                              <CheckCircle2 className="mt-0.5 h-4 w-4 flex-shrink-0 text-primary" />
                              <span>{item}</span>
                            </li>
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
        <section className="relative bg-gradient-to-b from-primary/5 via-white to-primary/5 py-20 lg:py-24">
          <div className="container mx-auto px-4">
            <div className="mb-14 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Tecnología avanzada
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Equipos de última tecnología
              </h2>
              <p className="mx-auto mt-3 max-w-3xl text-base text-muted-foreground sm:text-lg">
                Contamos con una infraestructura moderna que respalda cada procedimiento y garantiza
                diagnósticos confiables.
              </p>
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
              {equipos.map((equipo) => (
                <Card
                  key={equipo.title}
                  className="group overflow-hidden border-2 border-primary/10 bg-white/90 backdrop-blur-sm transition-all duration-300 hover:-translate-y-2 hover:border-primary/40 hover:shadow-xl"
                >
                  <CardContent className="relative space-y-3 p-6">
                    <div className="absolute right-0 top-0 h-20 w-20 rounded-bl-full bg-primary/5 transition-colors group-hover:bg-primary/10" />
                    <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-primary/20 to-primary/10 transition-transform group-hover:scale-110">
                      <Scan className="h-6 w-6 text-primary" />
                    </div>
                    <h3 className="relative z-10 text-[15px] font-bold group-hover:text-primary">
                      {equipo.title}
                    </h3>
                    <p className="relative z-10 text-sm leading-relaxed text-muted-foreground">
                      {equipo.description}
                    </p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </section>

        {/* CTA INTERMEDIA */}
        <section className="relative overflow-hidden bg-gradient-to-r from-primary via-primary/90 to-secondary py-20 text-center text-white">
          <div className="absolute inset-0 opacity-15">
            <div className="absolute left-10 top-10 h-40 w-40 rounded-full border-4 border-white/30" />
            <div className="absolute bottom-10 right-10 h-32 w-32 rounded-full border-4 border-white/30" />
            <div className="absolute left-1/2 top-1/2 h-64 w-64 -translate-x-1/2 -translate-y-1/2 rounded-full border-4 border-white/20" />
          </div>

          <div className="container relative z-10 mx-auto px-4">
            <div className="mx-auto max-w-3xl">
              <h2 className="text-balance text-3xl font-bold leading-tight sm:text-4xl lg:text-5xl">
                Agenda la salud de tu mascota{' '}
                <span className="relative inline-block">
                  hoy mismo<span className="absolute z-0 bottom-2 left-0 h-3 w-full bg-white/30" />
                </span>
              </h2>
              <p className="mt-4 text-pretty text-lg leading-relaxed opacity-95">
                Nuestro equipo está listo para brindarte atención profesional, cercana y humana.
              </p>
              <div className="mt-8 flex flex-col items-center justify-center gap-4 sm:flex-row sm:justify-center">
                <Button
                  size="lg"
                  className="group flex items-center justify-center gap-2 bg-white px-8 py-6 text-lg font-bold text-primary shadow-2xl transition-all duration-300 hover:scale-105 hover:bg-gray-100 hover:shadow-3xl"
                  onClick={() => navigate('/agendar-cita')}
                >
                  Solicitar cita ahora
                  <ArrowRight className="h-5 w-5 transition-transform group-hover:translate-x-1" />
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  className="flex items-center justify-center gap-2 border-white/70 bg-white/10 px-8 py-6 text-lg font-semibold text-white backdrop-blur-sm transition-all hover:bg-white/20"
                  onClick={() => scrollToSection('contacto')}
                >
                  Ver información de contacto
                </Button>
              </div>
            </div>
          </div>
        </section>

        {/* ESTADÍSTICAS ANIMADAS */}
        <section 
          ref={statsRef}
          className="relative overflow-hidden bg-gradient-to-r from-primary via-primary/90 to-secondary py-16 lg:py-20"
        >
          <div className="absolute inset-0 opacity-10">
            <div className="absolute left-1/4 top-0 h-96 w-96 rounded-full bg-white blur-3xl" />
            <div className="absolute right-1/4 bottom-0 h-96 w-96 rounded-full bg-white blur-3xl" />
          </div>
          <div className="container relative z-10 mx-auto px-4">
            <ScrollReveal className="mb-12 text-center">
              <h2 className="text-3xl font-bold text-white sm:text-4xl lg:text-5xl">
                Nuestra trayectoria en números
              </h2>
              <p className="mx-auto mt-3 max-w-2xl text-lg text-white/80">
                Años de experiencia respaldando la salud de miles de mascotas
              </p>
            </ScrollReveal>
            <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
              {estadisticas.map((stat, index) => (
                <ScrollReveal key={stat.label} delay={index * 100}>
                  <AnimatedStat
                    numero={stat.numero}
                    sufijo={stat.sufijo}
                    label={stat.label}
                    icon={stat.icon}
                    isVisible={statsVisible}
                  />
                </ScrollReveal>
              ))}
            </div>
          </div>
        </section>

        {/* EQUIPO MÉDICO */}
        <section
          id="equipo"
          className="relative bg-white py-20 lg:py-24"
        >
          <div className="container mx-auto px-4">
            <ScrollReveal className="mb-14 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Nuestro equipo
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Profesionales dedicados
              </h2>
              <p className="mx-auto mt-3 max-w-2xl text-base text-muted-foreground sm:text-lg">
                Un equipo de veterinarios altamente capacitados, comprometidos con el bienestar de tu mascota.
              </p>
            </ScrollReveal>

            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {equipoMedico.map((miembro, index) => (
                <ScrollReveal key={miembro.nombre} delay={index * 100}>
                  <Card className="group overflow-hidden border-2 border-primary/10 bg-white transition-all duration-300 hover:-translate-y-2 hover:border-primary/40 hover:shadow-xl">
                    <CardContent className="p-0">
                      <div className="relative aspect-square overflow-hidden bg-gradient-to-br from-primary/20 to-secondary/20">
                        <div className="absolute inset-0 flex items-center justify-center">
                          <div className="flex h-24 w-24 items-center justify-center rounded-full bg-white/30 backdrop-blur-sm transition-transform duration-300 group-hover:scale-110">
                            <Users className="h-12 w-12 text-primary" />
                          </div>
                        </div>
                        <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
                      </div>
                      <div className="p-5 text-center">
                        <h3 className="text-lg font-bold transition-colors group-hover:text-primary">
                          {miembro.nombre}
                        </h3>
                        <p className="text-sm font-medium text-primary">{miembro.cargo}</p>
                        <p className="mt-1 text-sm text-muted-foreground">{miembro.especialidad}</p>
                      </div>
                    </CardContent>
                  </Card>
                </ScrollReveal>
              ))}
            </div>
          </div>
        </section>

        {/* TESTIMONIOS */}
        <section
          id="testimonios"
          className="relative bg-gradient-to-b from-primary/5 via-white to-primary/5 py-20 lg:py-24"
        >
          <div className="container mx-auto px-4">
            <ScrollReveal className="mb-14 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Testimonios
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Lo que dicen nuestros clientes
              </h2>
              <p className="mx-auto mt-3 max-w-2xl text-base text-muted-foreground sm:text-lg">
                La confianza de miles de familias nos respalda
              </p>
            </ScrollReveal>

            <div className="mx-auto max-w-5xl">
              <Carousel
                opts={{
                  align: 'start',
                  loop: true,
                }}
                className="w-full"
              >
                <CarouselContent className="-ml-2 md:-ml-4">
                  {testimonios.map((testimonio, index) => (
                    <CarouselItem key={index} className="pl-2 md:basis-1/2 md:pl-4 lg:basis-1/2">
                      <Card className="h-full border-2 border-primary/10 bg-white transition-all duration-300 hover:border-primary/30 hover:shadow-lg">
                        <CardContent className="flex h-full flex-col p-6">
                          <Quote className="mb-4 h-8 w-8 text-primary/30" />
                          <p className="flex-1 text-sm leading-relaxed text-muted-foreground sm:text-base">
                            "{testimonio.texto}"
                          </p>
                          <div className="mt-6 flex items-center gap-4 border-t border-primary/10 pt-4">
                            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary/70">
                              <Users className="h-6 w-6 text-white" />
                            </div>
                            <div>
                              <p className="font-semibold">{testimonio.nombre}</p>
                              <p className="text-sm text-muted-foreground">{testimonio.mascota}</p>
                            </div>
                            <div className="ml-auto flex gap-0.5">
                              {Array.from({ length: testimonio.rating }).map((_, i) => (
                                <Star key={i} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                              ))}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    </CarouselItem>
                  ))}
                </CarouselContent>
                <div className="mt-6 flex justify-center gap-2">
                  <CarouselPrevious className="static translate-y-0" />
                  <CarouselNext className="static translate-y-0" />
                </div>
              </Carousel>
            </div>
          </div>
        </section>

        {/* VALORES */}
        <section
          id="valores"
          className="relative bg-white py-20 lg:py-24"
        >
          <div className="container mx-auto max-w-6xl px-4">
            <div className="mb-14 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Nuestra filosofía
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Nuestra filosofía y valores
              </h2>
              <p className="mx-auto mt-3 max-w-3xl text-base text-muted-foreground sm:text-lg">
                Creemos que los animales son miembros valiosos de la familia y merecen recibir
                atención médica excepcional. Nos enfocamos en fortalecer el vínculo entre humanos y
                animales a través de la educación, el cuidado y el amor.
              </p>
            </div>

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
              {valores.map((valor) => {
                const Icon = valor.icon;
                return (
                  <Card
                    key={valor.title}
                    className="group relative h-full border-2 border-primary/10 bg-white/90 text-center backdrop-blur-sm transition-all duration-300 hover:-translate-y-2 hover:border-primary/40 hover:shadow-xl"
                  >
                    <CardContent className="relative space-y-4 overflow-hidden p-6 lg:p-7">
                      <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/0 to-secondary/0 transition-all duration-500 group-hover:from-primary/5 group-hover:to-secondary/5" />
                      <div className="relative z-10 mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-primary/70 shadow-lg transition-all duration-300 group-hover:scale-110 group-hover:rotate-3">
                        <Icon className="h-8 w-8 text-white" />
                      </div>
                      <h3 className="relative z-10 text-base font-bold group-hover:text-primary">
                        {valor.title}
                      </h3>
                      <p className="relative z-10 text-sm leading-relaxed text-muted-foreground">
                        {valor.description}
                      </p>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>
        </section>

        {/* OBJETIVOS ESTRATÉGICOS */}
        <section className="relative bg-gradient-to-b from-primary/5 via-white to-primary/5 py-20 lg:py-24">
          <div className="container mx-auto max-w-4xl px-4">
            <div className="mb-10 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                Visión estratégica
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Objetivos estratégicos
              </h2>
            </div>

            <Card className="border-2 border-primary/10 bg-white/90 backdrop-blur-sm shadow-xl">
              <CardContent className="p-7 lg:p-10">
                <ul className="space-y-4">
                  {objetivosEstrategicos.map((objetivo) => (
                    <li
                      key={objetivo}
                      className="group flex items-start gap-4 rounded-lg p-4 transition-all duration-300 hover:bg-primary/5"
                    >
                      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary/70 shadow-md transition-transform group-hover:scale-110">
                        <CheckCircle2 className="h-5 w-5 text-white" />
                      </div>
                      <span className="pt-0.5 text-sm leading-relaxed text-muted-foreground group-hover:text-foreground sm:text-base">
                        {objetivo}
                      </span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          </div>
        </section>

        {/* FAQ - PREGUNTAS FRECUENTES */}
        <section
          id="faq"
          className="relative bg-white py-20 lg:py-24"
        >
          <div className="container mx-auto max-w-4xl px-4">
            <ScrollReveal className="mb-14 text-center">
              <Badge
                variant="outline"
                className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
              >
                FAQ
              </Badge>
              <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                Preguntas frecuentes
              </h2>
              <p className="mx-auto mt-3 max-w-2xl text-base text-muted-foreground sm:text-lg">
                Resolvemos tus dudas más comunes sobre nuestros servicios
              </p>
            </ScrollReveal>

            <ScrollReveal>
              <Card className="border-2 border-primary/10 bg-white shadow-xl">
                <CardContent className="p-6 lg:p-8">
                  <Accordion type="single" collapsible className="w-full">
                    {preguntasFrecuentes.map((faq, index) => (
                      <AccordionItem key={index} value={`item-${index}`} className="border-primary/10">
                        <AccordionTrigger className="text-left hover:text-primary hover:no-underline">
                          <div className="flex items-center gap-3">
                            <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary/10">
                              <HelpCircle className="h-4 w-4 text-primary" />
                            </div>
                            <span className="font-medium">{faq.pregunta}</span>
                          </div>
                        </AccordionTrigger>
                        <AccordionContent className="pl-11 text-muted-foreground">
                          {faq.respuesta}
                        </AccordionContent>
                      </AccordionItem>
                    ))}
                  </Accordion>
                </CardContent>
              </Card>
            </ScrollReveal>
          </div>
        </section>

        {/* NEWSLETTER */}
        <section className="relative overflow-hidden bg-gradient-to-r from-primary via-primary/90 to-secondary py-16 lg:py-20">
          <div className="absolute inset-0 opacity-10">
            <div className="absolute left-1/4 top-0 h-64 w-64 rounded-full bg-white blur-3xl" />
            <div className="absolute right-1/4 bottom-0 h-64 w-64 rounded-full bg-white blur-3xl" />
          </div>
          <div className="container relative z-10 mx-auto px-4">
            <div className="mx-auto max-w-2xl text-center">
              <ScrollReveal>
                <Mail className="mx-auto mb-4 h-12 w-12 text-white/80" />
                <h2 className="text-2xl font-bold text-white sm:text-3xl lg:text-4xl">
                  Suscríbete a nuestro boletín
                </h2>
                <p className="mt-3 text-white/80">
                  Recibe consejos de salud para tu mascota, promociones exclusivas y novedades de la clínica.
                </p>
                <form 
                  className="mt-8 flex flex-col gap-3 sm:flex-row sm:gap-2"
                  onSubmit={(e) => {
                    e.preventDefault();
                    // Handle newsletter subscription
                  }}
                >
                  <Input
                    type="email"
                    placeholder="Tu correo electrónico"
                    className="h-12 flex-1 bg-white/90 border-0 text-foreground placeholder:text-muted-foreground"
                    required
                  />
                  <Button
                    type="submit"
                    size="lg"
                    className="h-12 bg-white px-8 font-semibold text-primary hover:bg-white/90"
                  >
                    <Send className="mr-2 h-4 w-4" />
                    Suscribirse
                  </Button>
                </form>
                <p className="mt-4 text-xs text-white/60">
                  Respetamos tu privacidad. Puedes darte de baja en cualquier momento.
                </p>
              </ScrollReveal>
            </div>
          </div>
        </section>

        {/* CONTACTO */}
        <section
          id="contacto"
          className="relative overflow-hidden bg-white py-20 lg:py-24"
        >
          <div className="container mx-auto grid items-center gap-10 px-4 lg:grid-cols-2 lg:gap-12">
            <div className="relative z-10 space-y-7">
              <div>
                <Badge
                  variant="outline"
                  className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30"
                >
                  Contáctanos
                </Badge>
                <h2 className="text-balance text-3xl font-bold leading-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent sm:text-4xl lg:text-5xl">
                  Contacto y ubicación
                </h2>
                <p className="mt-3 text-base leading-relaxed text-muted-foreground sm:text-lg">
                  Estamos aquí para cuidar de tu mascota y acompañarte en cada etapa de su vida.
                  Agenda una cita o contáctanos para más información.
                </p>
              </div>

              <div className="space-y-4">
                <Card className="group border-2 border-primary/10 transition-all duration-300 hover:border-primary/40 hover:shadow-lg">
                  <CardContent className="flex items-start gap-4 p-6">
                    <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-primary/70 shadow-md transition-transform group-hover:scale-110">
                      <MapPin className="h-6 w-6 text-white" />
                    </div>
                    <div>
                      <h3 className="mb-1 text-lg font-bold transition-colors group-hover:text-primary">
                        Dirección
                      </h3>
                      <p className="text-sm leading-relaxed text-muted-foreground sm:text-base">
                        Calle 6 Norte # 14-26, [Ciudad]
                      </p>
                    </div>
                  </CardContent>
                </Card>

                <Card className="group border-2 border-primary/10 transition-all duration-300 hover:border-primary/40 hover:shadow-lg">
                  <CardContent className="flex items-start gap-4 p-6">
                    <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-secondary to-secondary/70 shadow-md transition-transform group-hover:scale-110">
                      <Phone className="h-6 w-6 text-white" />
                    </div>
                    <div>
                      <h3 className="mb-1 text-lg font-bold transition-colors group-hover:text-primary">
                        Teléfono / WhatsApp
                      </h3>
                      <p className="text-sm leading-relaxed text-muted-foreground sm:text-base">
                        +57 XXX XXX XXXX
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <div className="flex flex-col gap-3 sm:flex-row">
                <Button
                  className="group flex-1 px-8 py-6 text-lg shadow-lg transition-all duration-300 hover:scale-105 hover:shadow-xl"
                  size="lg"
                  onClick={() => navigate('/agendar-cita')}
                >
                  Solicitar cita
                  <ArrowRight className="ml-2 h-5 w-5 transition-transform group-hover:translate-x-1" />
                </Button>
                <Button
                  variant="outline"
                  className="flex-1 px-8 py-6 text-sm sm:text-base border-2 hover:bg-primary/5"
                  size="lg"
                  asChild
                >
                  <a href="tel:+573186160630" aria-label="Llamar a la clínica">
                    Llamar ahora
                  </a>
                </Button>
              </div>
            </div>

            {/* Mapa / visual */}
            <Card className="group overflow-hidden border-2 border-primary/10 shadow-2xl transition-all duration-300 hover:shadow-3xl">
              <CardContent className="relative flex aspect-square items-center justify-center bg-gradient-to-br from-primary/20 via-primary/10 to-secondary/20 p-0">
                <div className="absolute inset-0 opacity-25">
                  <div className="absolute left-10 top-10 h-32 w-32 rounded-full border-4 border-primary/30" />
                  <div className="absolute bottom-10 right-10 h-24 w-24 rounded-full border-4 border-primary/30" />
                </div>
                <div className="relative z-10 flex flex-col items-center px-8 text-center">
                  <div className="mb-4 flex h-24 w-24 items-center justify-center rounded-full bg-white/25 backdrop-blur-sm transition-transform group-hover:scale-110">
                    <MapPin className="h-12 w-12 text-primary" />
                  </div>
                  <h3 className="mb-1 text-lg font-semibold text-foreground">
                    Mapa interactivo
                  </h3>
                  <p className="max-w-xs text-sm text-muted-foreground">
                    Integra aquí tu mapa de Google Maps o el visor de ubicación de la clínica para
                    que los usuarios encuentren fácilmente cómo llegar.
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        </section>
      </main>

      {/* FOOTER */}
      <footer className="relative mt-16 overflow-hidden bg-gradient-to-b from-foreground to-foreground/95 py-14 text-white">
        <div className="absolute inset-0 opacity-5">
          <div className="absolute left-0 top-0 h-64 w-64 rounded-full border-4 border-white/30" />
          <div className="absolute bottom-0 right-0 h-48 w-48 rounded-full border-4 border-white/30" />
        </div>

        <div className="container relative z-10 mx-auto px-4">
          <div className="mb-10 grid gap-10 md:grid-cols-3">
            <div className="space-y-4">
              <div className="mb-3 flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary/60">
                  <Dog className="h-6 w-6 text-white" />
                </div>
                <div>
                  <h3 className="text-base font-semibold sm:text-lg">
                    Clínica Veterinaria Universitaria Humboldt
                  </h3>
                  <p className="text-xs uppercase tracking-[0.16em] text-gray-300">
                    Cuidando vidas, formando profesionales
                  </p>
                </div>
              </div>
              <p className="max-w-sm text-sm leading-relaxed text-gray-300">
                Servicios integrales de medicina veterinaria con respaldo académico y humano.
              </p>
            </div>

            <div className="space-y-4">
              <h4 className="text-lg font-bold">Contacto</h4>
              <div className="space-y-3 text-sm text-gray-300">
                <div className="flex items-center gap-3">
                  <Phone className="h-5 w-5 text-primary" />
                  <a
                    href="tel:+573186160630"
                    className="transition-colors hover:text-white"
                  >
                    +57 318 616 0630
                  </a>
                </div>
                <div className="flex items-center gap-3">
                  <MessageSquare className="h-5 w-5 text-primary" />
                  <a
                    href="mailto:contacto@vetclinic.com"
                    className="transition-colors hover:text-white"
                  >
                    contacto@vetclinic.com
                  </a>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h4 className="text-lg font-bold">Síguenos</h4>
              <div className="flex gap-3">
                <a
                  href="https://www.instagram.com/clinicaveterinariahumboldt/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group flex h-10 w-10 items-center justify-center rounded-full bg-white/10 transition-all duration-300 hover:scale-110 hover:bg-white/20"
                  aria-label="Facebook"
                >
                  <svg
                    className="h-5 w-5 text-white transition-colors group-hover:text-primary"
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
                  className="group flex h-10 w-10 items-center justify-center rounded-full bg-white/10 transition-all duration-300 hover:scale-110 hover:bg-white/20"
                  aria-label="Instagram"
                >
                  <svg
                    className="h-5 w-5 text-white transition-colors group-hover:text-primary"
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

          <div className="mt-8 border-t border-white/10 pt-6">
            <p className="text-center text-xs text-gray-400 sm:text-sm">
              © {new Date().getFullYear()} Clínica Veterinaria Universitaria Humboldt — Todos los
              derechos reservados.
            </p>
          </div>
        </div>
      </footer>

      {/* WHATSAPP FLOATING BUTTON */}
      <a
        href="https://wa.me/573186160630?text=Hola,%20me%20gustaría%20agendar%20una%20cita%20para%20mi%20mascota"
        target="_blank"
        rel="noopener noreferrer"
        className="fixed bottom-6 right-6 z-50 flex h-14 w-14 items-center justify-center rounded-full bg-[#25D366] text-white shadow-lg transition-all duration-300 hover:scale-110 hover:shadow-xl focus:outline-none focus:ring-2 focus:ring-[#25D366] focus:ring-offset-2"
        aria-label="Contactar por WhatsApp"
      >
        <svg 
          className="h-7 w-7" 
          fill="currentColor" 
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
        </svg>
      </a>

      {/* BACK TO TOP BUTTON */}
      <button
        onClick={scrollToTop}
        className={`fixed bottom-6 right-24 z-50 flex h-12 w-12 items-center justify-center rounded-full bg-primary text-white shadow-lg transition-all duration-300 hover:scale-110 hover:bg-primary/90 hover:shadow-xl focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 ${
          showBackToTop ? 'translate-y-0 opacity-100' : 'translate-y-16 opacity-0 pointer-events-none'
        }`}
        aria-label="Volver arriba"
      >
        <ChevronUp className="h-6 w-6" />
      </button>
    </div>
  );
}
