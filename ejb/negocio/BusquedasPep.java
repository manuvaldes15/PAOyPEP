package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.general.entidades.Genagencia;
import com.coop1.banksys.rhu.entidades.Rhuempleado;
import com.coop1.banksys.rhu.negocio.BusquedasRhu;
import com.coop1.soficoop.pln.dto.ReporteIniciativaDTO;
import com.coop1.soficoop.pln.dto.ResumenJerarquiaDTO;
import com.coop1.soficoop.pln.entidades.Plnaccidetplantrim;
import com.coop1.soficoop.pln.entidades.Plnaccioneval;
import com.coop1.soficoop.pln.entidades.Plnaccionsinplan;
import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import com.coop1.soficoop.pln.entidades.Plnaccionseguimiento;
import com.coop1.soficoop.pln.entidades.Plnpao;
import com.coop1.soficoop.pln.entidades.Plnpaoinic;
import com.coop1.soficoop.pln.entidades.Plnpaoinicdeta;
import com.coop1.soficoop.pln.entidades.Plnpep;
import com.coop1.soficoop.pln.entidades.Plnpepindicumpl;
import com.coop1.soficoop.pln.entidades.Plnpeplinestr;
import com.coop1.soficoop.pln.entidades.Plnpepobjetivo;
import com.coop1.soficoop.pln.entidades.Plnperspectivadeta;
import com.coop1.soficoop.pln.entidades.Plnperspectiva;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * EJB de Búsquedas para el Módulo de Planificación Estratégica (PEP) y Planes
 * Anuales Operativos (PAO).
 *
 * Organización jerárquica: 1. MÓDULO PEP (Plan Estratégico Participativo)
 * (Gerencia) └─ Perspectivas └─ Objetivos Estratégicos (OE) └─ Indicadores de
 * Cumplimiento └─ Líneas Estratégicas └─ Iniciativas (Solo para Coordinadores)
 *
 * 2. MÓDULO PAO (Plan Anual Operativo) (Coordinadores) └─ Acciones del PAO ├─
 * Plan Trimestral ├─ Evaluación └─ Seguimientos └─ Acciones No Planificadas
 *
 * 3. MÓDULOS TRANSVERSALES └─ Empleados └─ Consultas Nativas
 *
 */
@Stateless
public class BusquedasPep implements BusquedasPepLocal {
    
    private static final Logger LOG = Logger.getLogger(BusquedasPep.class.getName());

    @PersistenceContext(unitName = "BankSys-ejbPU")
    private EntityManager em;

    // ========================================================================
    // MÓDULO PEP - PLAN ESTRATÉGICO PARTICIPATIVO
    // ========================================================================
    /**
     * BUSCA PLANES ESTRATÉGICOS PARTICIPATIVOS (PEP).
     *
     * Funcionalidad: Permite buscar PEPs aplicando filtros dinámicos por año de
     * inicio, año de fin, estado e identificador del PEP. Ordena los resultados
     * por año de inicio de forma descendente.
     *
     * @param filtro Mapa de criterios de búsqueda utilizados para filtrar los
     * Planes Estratégicos.
     * @return Lista de {@link Plnpep} que cumplen con los criterios de
     * búsqueda.
     * @throws Exception Cuando ocurre un error durante la ejecución de la
     * consulta.
     */
    @Override
    public List<Plnpep> buscarPeps(Map filtro) throws Exception {
        List<Plnpep> lstPeps = new ArrayList<Plnpep>();
        try {
            StringBuilder jpql = new StringBuilder("SELECT OBJECT(p) FROM Plnpep p WHERE 1 = 1 ");

            if (filtro.containsKey("anioini") && filtro.get("anioini") != null) {
                jpql.append("AND p.anioini = :anioini ");
            }
            if (filtro.containsKey("aniofin") && filtro.get("aniofin") != null) {
                jpql.append("AND p.aniofin = :aniofin ");
            }
            boolean filtrarEstado = false;
            if (filtro.containsKey("estado")) {
                Object estadoVal = filtro.get("estado");
                if (estadoVal != null && !estadoVal.toString().equals("2")) {
                    jpql.append("AND p.estado = :estado ");
                    filtrarEstado = true;
                }
            }

            if (filtro.containsKey("idpep") && filtro.get("idpep") != null) {
                jpql.append("AND p.idpep = :idpep ");
            }

            jpql.append("ORDER BY p.anioini DESC");

            Query q = em.createQuery(jpql.toString());

            // Bypass de caché para obtener datos actualizados
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setHint("eclipselink.refresh", "true");

            if (filtro.containsKey("anioini") && filtro.get("anioini") != null) {
                q.setParameter("anioini", filtro.get("anioini"));
            }
            if (filtro.containsKey("aniofin") && filtro.get("aniofin") != null) {
                q.setParameter("aniofin", filtro.get("aniofin"));
            }

            if (filtrarEstado) {
                q.setParameter("estado", filtro.get("estado"));
            }

            if (filtro.containsKey("idpep") && filtro.get("idpep") != null) {
                q.setParameter("idpep", filtro.get("idpep"));
            }

            lstPeps = q.getResultList();
        } catch (NoResultException nre) {
            return new ArrayList<Plnpep>();
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Ejecutando búsqueda dinámica de PEPs con los filtros proporcionados.");
            Logger.getLogger(BusquedasPep.class.getName()).log(Level.SEVERE, "Error buscarPeps: {0}", ex);
            throw ex;
        }
        return lstPeps;
    }

    /**
     * VALIDA SI UN PEP TIENE PAOS ASOCIADOS.
     *
     * Funcionalidad: Permite validar si un Plan Estratégico tiene Planes
     * Anuales Operativos asociados antes de permitir su eliminación o
     * modificación.
     *
     * @param idPep Identificador del Plan Estratégico a consultar.
     * @return {@code true} si existen PAOs asociados, {@code false} en caso
     * contrario.
     */
    @Override
    public boolean tienePaosAsociados(BigInteger idPep) {
        try {
            String sql = "SELECT COUNT(p) FROM Plnpao p WHERE p.idpep.idpep = :idPep";
            Query q = em.createQuery(sql);
            q.setParameter("idPep", idPep);
            Long cantidad = (Long) q.getSingleResult();
            return cantidad > 0;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error de BD al verificar PAOs asociados para el PEP ID: " + idPep, e);
            e.printStackTrace();
            return false;
        }
    }

    // ========================================================================
    // MÓDULO PERSPECTIVAS (Nivel 1 de jerarquía PEP)
    // ========================================================================
    /**
     * BUSCA TODAS LAS PERSPECTIVAS (CATÁLOGO MAESTRO).
     *
     * Funcionalidad: Obtiene todas las Perspectivas activas e inactivas para su
     * uso en controles tipo ComboBox. Ordena los resultados alfabéticamente por
     * nombre.
     *
     * @return Lista de {@link Plnperspectiva} registradas en el sistema.
     * @throws Exception Cuando ocurre un error durante la consulta de
     * Perspectivas.
     */
    @Override
    public List<Plnperspectiva> buscarTodasLasMaestras() throws Exception {
        try {
            Query q = em.createQuery("SELECT m FROM Plnperspectiva m WHERE m.idmaestro IS NOT NULL AND m.nombre IS NOT NULL ORDER BY m.idmaestro ASC");
            q.setHint("eclipselink.refresh", true);
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Plnperspectiva>();
        }
    }

    /**
     * BUSCA UNA PERSPECTIVA POR IDENTIFICADOR.
     *
     * Funcionalidad: Obtiene una Perspectiva del catálogo maestro a partir de
     * su identificador para establecer relaciones con otros registros.
     *
     * @param idMaestro Identificador de la Perspectiva a consultar.
     * @return Objeto {@link Plnperspectiva} si existe, o {@code null} si no se
     * encuentra.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public Plnperspectiva buscarMaestroPorId(BigInteger idMaestro) throws Exception {
        try {
            Query q = em.createQuery("SELECT m FROM Plnperspectiva m WHERE m.idmaestro = :id ORDER BY m.idmaestro ASC");
            q.setParameter("id", idMaestro);
            q.setHint("eclipselink.refresh", true);
            return (Plnperspectiva) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * BUSCA LAS PERSPECTIVAS ASIGNADAS A UN PEP.
     *
     * Funcionalidad: Obtiene la lista de Perspectivas asociadas a un Plan
     * Estratégico para su visualización y gestión en la tabla de asignación.
     *
     * @param idPep Identificador del Plan Estratégico a consultar.
     * @return Lista de {@link Plnperspectivadeta} con las Perspectivas
     * asociadas al PEP.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnperspectivadeta> buscarPerspectivasPorPep(BigInteger idPep) throws Exception {
        try {
            Query q = em.createQuery(
                    "SELECT p FROM Plnperspectivadeta p "
                    + "WHERE p.idpep.idpep = :idPep ");
            q.setParameter("idPep", idPep);
            q.setHint("eclipselink.refresh", true);
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Plnperspectivadeta>();
        }
    }

    /**
     * BUSCA EL USO DE UNA PERSPECTIVA MAESTRA EN PLANES ESTRATÉGICOS.
     *
     * Funcionalidad: Permite validar si una Perspectiva del catálogo maestro
     * está siendo utilizada en uno o más Planes Estratégicos antes de realizar
     * su eliminación.
     *
     * @param idMaestro Identificador de la Perspectiva Maestra a consultar.
     * @return Lista de {@link Plnperspectivadeta} que representan las
     * asignaciones de la Perspectiva en Planes Estratégicos.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnperspectivadeta> buscarPerspectivasPorMaestro(BigInteger idMaestro) throws Exception {
        try {
            Query q = em.createQuery("SELECT p FROM Plnperspectivadeta p WHERE p.perspectivaMaestra.idmaestro = :idMaestro");
            q.setParameter("idMaestro", idMaestro);
            q.setHint("eclipselink.refresh", true);
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Plnperspectivadeta>();
        }
    }

    // ========================================================================
    // MÓDULO OBJETIVOS ESTRATÉGICOS (OE) - Nivel 2 de jerarquía PEP
    // ========================================================================
    /**
     * BUSCA OBJETIVOS ESTRATÉGICOS POR PERSPECTIVA.
     *
     * Funcionalidad: Obtiene los Objetivos Estratégicos asociados a una
     * Perspectiva específica para su gestión y visualización. Ordena por código
     * para mantener secuencia lógica (1, 2, 3...).
     *
     * @param idPerspectiva Identificador de la Perspectiva a consultar.
     * @return Lista de {@link Plnpepobjetivo} asociados a la Perspectiva
     * indicada.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnpepobjetivo> buscarObjetivosPorPerspectiva(BigInteger idPerspectiva) throws Exception {
        List<Plnpepobjetivo> lista = new ArrayList<Plnpepobjetivo>();
        try {
            String sql = "SELECT o FROM Plnpepobjetivo o WHERE o.idperspectiva.idperspectiva = :id ORDER BY o.codigooe ASC";

            Query q = em.createQuery(sql);
            q.setParameter("id", idPerspectiva);

            // Bypass de caché
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setHint("eclipselink.refresh", "true");

            lista = q.getResultList();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al consultar Objetivos Estrategicos de la Perspectiva ID: " + idPerspectiva, e);
            e.printStackTrace();
            return new ArrayList<Plnpepobjetivo>();
        }
        return lista;
    }

    /**
     * BUSCA UN OBJETIVO ESTRATÉGICO POR IDENTIFICADOR.
     *
     * Funcionalidad: Obtiene un Objetivo Estratégico específico a partir de su
     * identificador único.
     *
     * @param id Identificador del Objetivo Estratégico a consultar.
     * @return Objeto {@link Plnpepobjetivo} correspondiente al identificador.
     * @throws Exception Cuando ocurre un error durante la consulta del
     * Objetivo.
     */
    @Override
    public Plnpepobjetivo buscarObjetivoPorId(BigInteger id) throws Exception {
        Query q = em.createQuery("SELECT o FROM Plnpepobjetivo o WHERE o.idobjetivo = :id");
        q.setParameter("id", id);
        q.setHint("eclipselink.refresh", "true");
        return (Plnpepobjetivo) q.getSingleResult();
    }

    // ========================================================================
    // MÓDULO INDICADORES DE CUMPLIMIENTO - Nivel 3 de jerarquía PEP
    // ========================================================================
    /**
     * BUSCA INDICADORES DE CUMPLIMIENTO POR OBJETIVO ESTRATÉGICO.
     *
     * Funcionalidad: Obtiene los Indicadores de Cumplimiento asociados a un
     * Objetivo Estratégico específico. Ordena por código del indicador.
     *
     * @param idObjetivo Identificador del Objetivo Estratégico a consultar.
     * @return Lista de {@link Plnpepindicumpl} asociados al Objetivo indicado.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnpepindicumpl> buscarIndicadoresPorObjetivo(BigInteger idObjetivo) throws Exception {
        try {
            Query q = em.createQuery("SELECT i FROM Plnpepindicumpl i WHERE i.idobjetivo.idobjetivo = :idPadre ORDER BY i.codigoindicador ASC");
            q.setParameter("idPadre", idObjetivo);
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Plnpepindicumpl>();
        }
    }

    /**
     * BUSCA UN INDICADOR DE CUMPLIMIENTO POR IDENTIFICADOR.
     *
     * Funcionalidad: Obtiene un Indicador de Cumplimiento específico a partir
     * de su identificador único.
     *
     * @param id Identificador del Indicador de Cumplimiento a consultar.
     * @return Objeto {@link Plnpepindicumpl} correspondiente al identificador.
     * @throws Exception Cuando ocurre un error durante la consulta del
     * Indicador.
     */
    @Override
    public Plnpepindicumpl buscarIndicadorPorId(BigInteger id) throws Exception {
        Query q = em.createQuery("SELECT i FROM Plnpepindicumpl i WHERE i.idindicadorcump = :id");
        q.setParameter("id", id);
        q.setHint("eclipselink.refresh", "true");
        return (Plnpepindicumpl) q.getSingleResult();
    }

    // ========================================================================
    // MÓDULO LÍNEAS ESTRATÉGICAS - Nivel 4 de jerarquía PEP
    // ========================================================================
    /**
     * BUSCA LÍNEAS ESTRATÉGICAS POR INDICADOR DE CUMPLIMIENTO.
     *
     * Funcionalidad: Obtiene las Líneas Estratégicas asociadas a un Indicador
     * de Cumplimiento específico. Ordena por identificador de estrategia.
     *
     * @param idIndicador Identificador del Indicador de Cumplimiento a
     * consultar.
     * @return Lista de {@link Plnpeplinestr} asociadas al Indicador indicado.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnpeplinestr> buscarEstrategiasPorIndicador(BigInteger idIndicador)
            throws Exception {
        try {
            String jpql = "SELECT e FROM Plnpeplinestr e "
                    + "WHERE e.idindicadorcump.idindicadorcump = :idPadre "
                    + "ORDER BY e.idestrategia ASC";
            Query q = em.createQuery(jpql);
            q.setParameter("idPadre", idIndicador);
            q.setHint("eclipselink.refresh", "true");        // ← agregar
            q.setHint("javax.persistence.cache.storeMode", "REFRESH"); // ← agregar
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<Plnpeplinestr>();
        }
    }

    /**
     * BUSCA UNA LÍNEA ESTRATÉGICA POR IDENTIFICADOR.
     *
     * Funcionalidad: Obtiene una Línea Estratégica específica a partir de su
     * identificador único.
     *
     * @param id Identificador de la Línea Estratégica a consultar.
     * @return Objeto {@link Plnpeplinestr} si existe, o {@code null} si no se
     * encuentra.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public Plnpeplinestr buscarEstrategiaPorId(BigInteger id) throws Exception {
        try {
            Query q = em.createQuery("SELECT e FROM Plnpeplinestr e WHERE e.idestrategia = :id");
            q.setParameter("id", id);
            q.setHint("eclipselink.refresh", "true");
            return (Plnpeplinestr) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * OBTIENE UN RESUMEN DE LA JERARQUÍA POR LÍNEA ESTRATÉGICA.
     *
     * Funcionalidad: Construye un resumen jerárquico que incluye Perspectiva,
     * Objetivo Estratégico, Indicador de Cumplimiento y Línea Estratégica, a
     * partir del identificador de la Estrategia. El resultado se devuelve
     * encapsulado en un DTO.
     *
     * @param idEstrategia Identificador de la Línea Estratégica a consultar.
     * @return {@link ResumenJerarquiaDTO} con la información jerárquica
     * asociada, o un DTO vacío si no existen datos.
     * @throws Exception Cuando ocurre un error durante la consulta o el armado
     * del resumen.
     */
    @Override
    public ResumenJerarquiaDTO obtenerResumenPorEstrategia(BigInteger idEstrategia) throws Exception {
        ResumenJerarquiaDTO dto = new ResumenJerarquiaDTO();

        if (idEstrategia == null) {
            return dto;
        }

        try {
            String jpql = "SELECT s FROM Plnpeplinestr s "
                    + "JOIN s.idindicadorcump i "
                    + "JOIN i.idobjetivo o "
                    + "JOIN o.idperspectiva p "
                    + "JOIN p.perspectivaMaestra m "
                    + "WHERE s.idestrategia = :id";

            Query q = em.createQuery(jpql);
            q.setParameter("id", idEstrategia);

            List<Plnpeplinestr> list = q.getResultList();

            if (!list.isEmpty()) {
                Plnpeplinestr s = list.get(0);

                dto.setEstrategia(s.getDescrip());

                if (s.getIdindicadorcump() != null) {
                    dto.setIndicador(s.getIdindicadorcump().getCodigoindicador() + " - " + s.getIdindicadorcump().getDescrip());

                    if (s.getIdindicadorcump().getIdobjetivo() != null) {
                        dto.setObjetivo(s.getIdindicadorcump().getIdobjetivo().getCodigooe() + " - " + s.getIdindicadorcump().getIdobjetivo().getDescrip());

                        if (s.getIdindicadorcump().getIdobjetivo().getIdperspectiva() != null
                                && s.getIdindicadorcump().getIdobjetivo().getIdperspectiva().getPerspectivaMaestra() != null) {

                            dto.setPerspectiva(s.getIdindicadorcump().getIdobjetivo().getIdperspectiva().getPerspectivaMaestra().getNombre());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al armar el DTO de Resumen Jerárquico para la Estrategia ID: " + idEstrategia, e);
            dto.setAccion("Error al cargar jerarquía.");
        }

        return dto;
    }

    // ========================================================================
    // MÓDULO INICIATIVAS
    // ========================================================================
    /**
     * BUSCA INICIATIVAS POR INDICADOR DE CUMPLIMIENTO.
     *
     * Funcionalidad: Obtiene las Iniciativas asociadas a un Indicador de
     * Cumplimiento específico.
     *
     * @param idIndicador Identificador del Indicador de Cumplimiento a
     * consultar.
     * @return Lista de {@link Plnpaoinic} asociadas al Indicador indicado.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnpaoinic> buscarIniciativasPorIndicador(BigInteger idIndicador) throws Exception {
        try {
            if (idIndicador == null) {
                return new ArrayList<>();
            }

            String jpql = "SELECT i FROM Plnpaoinic i WHERE i.idindicadorcump.idindicadorcump = :id ORDER BY i.idindicadorcump.codigoindicador, i.idiniciativa";
            Query q = em.createQuery(jpql);
            q.setParameter("id", idIndicador);
            return q.getResultList();

        } catch (Exception e) {
            throw new Exception("Error buscando iniciativas por indicador: " + e.getMessage());
        }
    }

    /**
     * BUSCA INICIATIVAS POR PLAN ESTRATÉGICO (PEP).
     *
     * Funcionalidad: Obtiene todas las Iniciativas que pertenecen a un Plan
     * Estratégico navegando la jerarquía completa: Iniciativa → Indicador →
     * Objetivo → Perspectiva → PEP.
     *
     * @param idPep Identificador del Plan Estratégico a consultar.
     * @return Lista de {@link Plnpaoinic} asociadas al PEP indicado.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnpaoinic> buscarIniciativasPorPep(BigInteger idPep) throws Exception {
        try {
            if (idPep == null) {
                return new ArrayList<>();
            }

            String jpql = "SELECT i FROM Plnpaoinic i "
                    + "WHERE i.idindicadorcump.idobjetivo.idperspectiva.idpep.idpep = :idPep "
                    + "ORDER BY i.idindicadorcump.codigoindicador";

            Query q = em.createQuery(jpql);
            q.setParameter("idPep", idPep);
            return q.getResultList();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en la consulta de iniciativas vinculadas al PEP ID: " + idPep, e);
            return new ArrayList<>();
        }
    }

    /**
     * BUSCA UNA INICIATIVA POR IDENTIFICADOR.
     *
     * Funcionalidad: Obtiene una Iniciativa específica a partir de su
     * identificador único.
     *
     * @param idIniciativa Identificador de la Iniciativa a consultar.
     * @return Objeto {@link Plnpaoinic} si existe, o {@code null} si no se
     * encuentra.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public Plnpaoinic buscarIniciativaPorId(BigInteger idIniciativa) throws Exception {
        try {
            if (idIniciativa == null) {
                return null;
            }
            return em.find(Plnpaoinic.class, idIniciativa);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error buscando iniciativa por ID: " + e.getMessage());
        }
    }

    /**
     * BUSCA ACCIONES DEL PAO YA VINCULADAS A UNA INICIATIVA.
     *
     * Funcionalidad: Obtiene las Acciones del PAO que están actualmente
     * asociadas a una Iniciativa específica a través de la tabla
     * PLNPAOINICDETA.
     *
     * @param idIniciativa Identificador de la Iniciativa a consultar.
     * @return Lista de {@link Plnacciondeta} vinculadas a la Iniciativa.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @PermitAll
    @Override
    public List<Plnacciondeta> buscarAccionesPorIniciativa(BigInteger idIniciativa) throws Exception {
        String jpql = "SELECT v.idacciondeta FROM Plnpaoinicdeta v WHERE v.idiniciativa.idiniciativa = :idIni";
        Query q = em.createQuery(jpql);
        q.setParameter("idIni", idIniciativa);
        return q.getResultList();
    }

    /**
     * BUSCA ACCIONES DEL PAO DISPONIBLES PARA VINCULAR A UNA INICIATIVA.
     *
     * Funcionalidad: Obtiene las Acciones del PAO que aún no están vinculadas a
     * ninguna Iniciativa y que pertenecen al mismo Indicador de Cumplimiento.
     *
     * @param idIndicador Identificador del Indicador de Cumplimiento.
     * @param idPao Identificador del Plan Anual Operativo.
     * @return Lista de {@link Plnacciondeta} disponibles para vincular.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @PermitAll
    @Override
    public List<Plnacciondeta> buscarAccionesDisponiblesParaIniciativa(BigInteger idIndicador, BigInteger idPao) throws Exception {
        String jpql = "SELECT a FROM Plnacciondeta a "
                + "WHERE a.idpao.idpao = :idPao "
                + "AND a.idestrategia.idindicadorcump.idindicadorcump = :idInd "
                + "AND a.idaccionpao NOT IN (SELECT v.idacciondeta.idaccionpao FROM Plnpaoinicdeta v)";

        Query q = em.createQuery(jpql);
        q.setParameter("idPao", idPao);
        q.setParameter("idInd", idIndicador);
        return q.getResultList();
    }

    /**
     * BUSCA EL VÍNCULO ENTRE UNA INICIATIVA Y UNA ACCIÓN DEL PAO.
     *
     * Funcionalidad: Obtiene el registro de vinculación entre una Iniciativa y
     * una Acción del PAO para su eliminación o consulta.
     *
     * @param idIniciativa Identificador de la Iniciativa.
     * @param idAccion Identificador de la Acción del PAO.
     * @return Objeto {@link Plnpaoinicdeta} si existe el vínculo, o
     * {@code null} si no existe.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public Plnpaoinicdeta buscarVinculo(BigInteger idIniciativa, BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT v FROM Plnpaoinicdeta v WHERE v.idiniciativa.idiniciativa = :idIni AND v.idacciondeta.idaccionpao = :idAcc";
            Query q = em.createQuery(jpql);
            q.setParameter("idIni", idIniciativa);
            q.setParameter("idAcc", idAccion);
            return (Plnpaoinicdeta) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Busca iniciativas de un indicador filtradas por coordinador/dueño. Cada
     * usuario solo ve sus propias iniciativas.
     */
    public List<Plnpaoinic> buscarIniciativasPorIndicadorYCoordinador(
            BigInteger idIndicador, BigInteger codCoordinador) {
        try {
            return em.createQuery(
                    "SELECT i FROM Plnpaoinic i "
                    + "WHERE i.idindicadorcump.idindicadorcump = :idInd "
                    + "AND i.idcoordinador.codemp = :codCoord "
                    + "ORDER BY i.idiniciativa",
                    Plnpaoinic.class)
                    .setParameter("idInd", idIndicador)
                    .setParameter("codCoord", codCoordinador)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al filtrar iniciativas por coordinador ID: " + codCoordinador, e);
            return new ArrayList<>();
        }
    }

    /**
     * Busca iniciativas de un PEP filtradas por coordinador. Usado para el
     * reporte ejecutivo — solo muestra las del usuario actual.
     */
    public List<Plnpaoinic> buscarIniciativasPorPepYCoordinador(
            BigInteger idPep, BigInteger codCoordinador) {
        try {
            return em.createQuery(
                    "SELECT i FROM Plnpaoinic i "
                    + "WHERE i.idindicadorcump.idobjetivo.idperspectiva.idpep.idpep = :idPep "
                    + "AND i.idcoordinador.codemp = :codCoord "
                    + "ORDER BY i.idiniciativa",
                    Plnpaoinic.class)
                    .setParameter("idPep", idPep)
                    .setParameter("codCoord", codCoordinador)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al filtrar iniciativas por coordinador ID: " + codCoordinador, e);
            return new ArrayList<>();
        }
    }

 
    /**
 * Retorna todas las iniciativas del coordinador con conteo
 * de acciones vinculadas y PAOs distintos que las usan.
 */
public List<ReporteIniciativaDTO> buscarResumenIniciativasPorCoordinador(
        BigInteger codCoordinador) {

    List<ReporteIniciativaDTO> resultado = new ArrayList<>();

    try {
        LOG.log(Level.INFO, "Generando Reporte DTO de Iniciativas para el Coordinador ID: {0}", codCoordinador);
        
        if (codCoordinador == null) {
            return resultado;
        }

        List<Plnpaoinic> iniciativas = em.createQuery(
                "SELECT i FROM Plnpaoinic i "
                + "WHERE i.idcoordinador.codemp = :cod "
                + "ORDER BY i.idiniciativa",
                Plnpaoinic.class)
                .setParameter("cod", codCoordinador)
                .getResultList();

        for (Plnpaoinic inic : iniciativas) {

            // ✅ Constructor existente del DTO carga la jerarquía
            ReporteIniciativaDTO dto = new ReporteIniciativaDTO(inic);

            // Total de acciones vinculadas a esta iniciativa
            Long totalAcciones = 0L;
            try {
                totalAcciones = em.createQuery(
                        "SELECT COUNT(d) FROM Plnpaoinicdeta d "
                        + "WHERE d.idiniciativa.idiniciativa = :idInic",
                        Long.class)
                        .setParameter("idInic", inic.getIdiniciativa())
                        .getSingleResult();
            } catch (Exception ex) {
                ex.printStackTrace();
                LOG.log(Level.SEVERE, "Error al contar acciones de la iniciativa ID: " + inic.getIdiniciativa(), ex);
            }

            // Total de PAOs distintos que tienen acciones en esta iniciativa
            Long totalPaos = 0L;
            try {
                totalPaos = em.createQuery(
                        "SELECT COUNT(DISTINCT d.idacciondeta.idpao.idpao) "
                        + "FROM Plnpaoinicdeta d "
                        + "WHERE d.idiniciativa.idiniciativa = :idInic",
                        Long.class)
                        .setParameter("idInic", inic.getIdiniciativa())
                        .getSingleResult();
            } catch (Exception ex) {
                ex.printStackTrace();
                LOG.log(Level.SEVERE, "Error al contar PAOs distintos de la iniciativa ID: " + inic.getIdiniciativa(), ex);

            }

            dto.setTotalAcciones(totalAcciones.intValue());
            dto.setTotalPaos(totalPaos.intValue());

            resultado.add(dto);
        }

    } catch (Exception e) {
        e.printStackTrace();
        LOG.log(Level.SEVERE, "Error al construir el reporte de iniciativas para el coordinador ID: " + codCoordinador, e);
    }

    return resultado;
}

    

    // ========================================================================
    // MÓDULO PAO - PLAN ANUAL OPERATIVO
    // ========================================================================
    /**
     * BUSCA TODOS LOS PLANES ANUALES OPERATIVOS (PAO).
     *
     * Funcionalidad: Obtiene la lista completa de PAO registrados en el
     * sistema, ordenados por año de forma descendente.
     *
     * @return Lista de {@link Plnpao} disponibles en el sistema.
     * @throws Exception Cuando ocurre un error durante la consulta de PAO.
     */
    @Override
    public List<Plnpao> buscarTodosLosPaos() throws Exception {
        try {
            return em.createQuery("SELECT p FROM Plnpao p ORDER BY p.anio DESC").getResultList();
        } catch (Exception e) {
            return new ArrayList<Plnpao>();
        }
    }

    /**
     * BUSCA PLANES ANUALES OPERATIVOS (PAO) POR COORDINADOR.
     *
     * Funcionalidad: Obtiene los PAO asociados a un coordinador específico,
     * ordenados por año de forma descendente.
     *
     * @param codemp Identificador del coordinador a consultar.
     * @return Lista de {@link Plnpao} asociados al coordinador indicado.
     * @throws Exception Cuando ocurre un error durante la consulta de PAO por
     * coordinador.
     */
    @Override
    public List<Plnpao> buscarPaosPorCoordinador(BigInteger codemp) throws Exception {
        try {
            Query q = em.createQuery(
                    "SELECT p FROM Plnpao p "
                    + "WHERE p.idcoordinador.codemp = :codemp "
                    + "ORDER BY p.anio DESC");
            q.setParameter("codemp", codemp);
            q.setHint("eclipselink.refresh", "true");
            return q.getResultList();
        } catch (Exception e) {
            return new ArrayList<Plnpao>();
        }
    }

    /**
     * BUSCA PLANES ANUALES OPERATIVOS (PAO) POR FILTROS.
     *
     * Funcionalidad: Permite buscar PAO aplicando filtros dinámicos por
     * coordinador, año y Plan Estratégico (PEP). Ordena los resultados por año
     * de forma descendente.
     *
     * @param codEmp Identificador del coordinador del PAO.
     * @param anio Año del PAO a filtrar (opcional).
     * @param idPep Identificador del Plan Estratégico asociado (opcional).
     * @return Lista de {@link Plnpao} que cumplen con los filtros indicados.
     * @throws Exception Cuando ocurre un error durante la consulta de PAO.
     */
    @Override
    public List<Plnpao> buscarPaosPorFiltros(BigInteger codEmp, Integer anio, BigInteger idPep) throws Exception {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT p FROM Plnpao p WHERE p.idcoordinador.codemp = :codEmp ");
        if (anio != null && anio > 0) {
            jpql.append("AND p.anio = :anio ");
        }
        if (idPep != null && idPep.compareTo(BigInteger.ZERO) > 0) {
            jpql.append("AND p.idpep.idpep = :idPep ");
        }
        jpql.append("ORDER BY p.anio DESC");
        Query q = em.createQuery(jpql.toString());
        q.setParameter("codEmp", codEmp);

        if (anio != null && anio > 0) {
            q.setParameter("anio", anio);
        }
        if (idPep != null && idPep.compareTo(BigInteger.ZERO) > 0) {
            q.setParameter("idPep", idPep);
        }
        return q.getResultList();
    }

    /**
     * BUSCA PLANES ANUALES OPERATIVOS (PAO) POR PLAN ESTRATÉGICO (PEP).
     *
     * Funcionalidad: Obtiene todos los PAOs asociados a un Plan Estratégico
     * específico.
     *
     * @param idPep Identificador del Plan Estratégico a consultar.
     * @return Lista de {@link Plnpao} asociados al PEP indicado.
     */
    @Override
    public List<Plnpao> buscarPaosPorPep(BigInteger idPep) {
        try {
            String sql = "SELECT p FROM Plnpao p WHERE p.idpep.idpep = :idPep ORDER BY p.anio DESC";

            Query q = em.createQuery(sql);
            q.setParameter("idPep", idPep);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");

            return q.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error en la consulta de filtrado de PAOs", e);
            return new ArrayList<Plnpao>();
        }
    }

    /**
     * BUSCA UN PAO POR DEPARTAMENTO Y AÑO.
     *
     * Funcionalidad: Obtiene el PAO del año especificado donde el coordinador
     * tenga un puesto que pertenezca al departamento solicitado.
     *
     * @param idDepto Identificador del Departamento.
     * @param anio Año del PAO a consultar.
     * @return Objeto {@link Plnpao} si existe, o {@code null} si no se
     * encuentra.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public Plnpao buscarPaoPorDepartamento(BigInteger idDepto, Integer anio) throws Exception {
        try {
            String jpql = "SELECT p FROM Plnpao p "
                    + "WHERE p.idcoordinador.puesto.rhudepto.iddepto = :idDepto "
                    + "AND p.anio = :anio";

            Query q = em.createQuery(jpql);
            q.setParameter("idDepto", idDepto);
            q.setParameter("anio", anio);

            List<Plnpao> lista = q.getResultList();
            if (!lista.isEmpty()) {
                return lista.get(0);
            }
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en la consulta de filtrado de PAOs", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * BUSCA UN PAO POR JEFE INMEDIATO Y AÑO.
     *
     * Funcionalidad: Obtiene el PAO donde el Coordinador sea el Jefe del
     * empleado en el año especificado.
     *
     * @param codJefe Identificador del Jefe (Coordinador del PAO).
     * @param anio Año del PAO a consultar.
     * @return Objeto {@link Plnpao} si existe, o {@code null} si no se
     * encuentra.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public Plnpao buscarPaoPorJefe(BigInteger codJefe, Integer anio) throws Exception {
        try {
            String jpql = "SELECT p FROM Plnpao p "
                    + "WHERE p.idcoordinador.codemp = :codJefe "
                    + "AND p.anio = :anio "
                    + "ORDER BY p.idpao DESC";

            Query q = em.createQuery(jpql);
            q.setParameter("codJefe", codJefe);
            q.setParameter("anio", anio);

            List<Plnpao> lista = q.getResultList();
            if (!lista.isEmpty()) {
                return lista.get(0);
            }
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en la consulta de filtrado de PAOs", e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * BUSCA PLANES ANUALES OPERATIVOS (PAO) CON FILTROS DINÁMICOS AVANZADOS.
     *
     * Funcionalidad: Permite buscar PAO aplicando filtros por Plan Estratégico,
     * año, agencia, departamento y nombre del coordinador.
     *
     * @param filtros Mapa con los criterios de búsqueda: idPep, anio, codAgen,
     * codDepto, nombreCoord.
     * @return Lista de {@link Plnpao} que cumplen con los filtros indicados.
     * @throws Exception Cuando ocurre un error durante la consulta.
     */
    @Override
    public List<Plnpao> buscarPaosDinamico(Map<String, Object> filtros) throws Exception {
        LOG.log(Level.INFO, "Ejecutando búsqueda avanzada de PAOs. Filtros recibidos: Año={0}, PEP={1}, Coordinador={2}", 
        new Object[]{filtros.get("anio"), filtros.get("idPep"), filtros.get("nombreCoord")});
        
        StringBuilder jpql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();

        jpql.append("SELECT p FROM Plnpao p ");
        jpql.append("JOIN FETCH p.idpep ");
        jpql.append("JOIN p.idcoordinador c ");
        jpql.append("JOIN c.persona per ");
        jpql.append("WHERE 1=1 ");

        if (filtros.containsKey("idPep")) {
            jpql.append("AND p.idpep.idpep = :idPep ");
            params.put("idPep", filtros.get("idPep"));
        }

        if (filtros.containsKey("anio")) {
            jpql.append("AND p.anio = :anio ");
            params.put("anio", filtros.get("anio"));
        }

        if (filtros.containsKey("codAgen")) {
            jpql.append("AND c.agencia.codagen = :codAgen ");
            params.put("codAgen", filtros.get("codAgen"));
        }

        if (filtros.containsKey("codDepto")) {
            jpql.append("AND c.puesto.rhudepto.rhudeptoPK.coddepto = :codDepto ");
            params.put("codDepto", filtros.get("codDepto"));
        }

        if (filtros.containsKey("nombreCoord")) {
            jpql.append("AND UPPER(per.nomcom) LIKE :nombreCoord ");
            String nombre = filtros.get("nombreCoord").toString().toUpperCase();
            if (!nombre.contains("%")) {
                nombre = "%" + nombre + "%";
            }
            params.put("nombreCoord", nombre);
        }

        jpql.append("ORDER BY p.anio DESC, p.nombrepao ASC");

        Query q = em.createQuery(jpql.toString());

        for (Map.Entry<String, Object> param : params.entrySet()) {
            q.setParameter(param.getKey(), param.getValue());
        }

        return q.getResultList();
    }

    // ========================================================================
    // MÓDULO ACCIONES DEL PAO
    // ========================================================================
    /**
     * BUSCA ACCIONES DEL PAO POR PAO Y LÍNEA ESTRATÉGICA.
     *
     * Funcionalidad: Obtiene las Acciones del PAO asociadas a un Plan Anual
     * Operativo y a una Línea Estratégica específica. Ordena los resultados
     * alfabéticamente por descripción.
     *
     * @param idPao Identificador del Plan Anual Operativo.
     * @param idEstrategia Identificador de la L��nea Estratégica.
     * @return Lista de {@link Plnacciondeta} que cumplen con los criterios
     * indicados.
     * @throws Exception Cuando ocurre un error durante la consulta de Acciones
     * del PAO.
     */
    @Override
    public List<Plnacciondeta> buscarAccionesPorPaoYEstrategia(BigInteger idPao, BigInteger idEstrategia) throws Exception {
        try {
            Query q = em.createQuery(
                    "SELECT a FROM Plnacciondeta a "
                    + "WHERE a.idpao.idpao = :idPao "
                    + "AND a.idestrategia.idestrategia = :idEst "
                    + "ORDER BY a.descrip ASC");
            q.setParameter("idPao", idPao);
            q.setParameter("idEst", idEstrategia);
            q.setHint("eclipselink.refresh", "true");
            return q.getResultList();
        } catch (Exception e) {
            return new ArrayList<Plnacciondeta>();
        }
    }

    /**
     * BUSCA ACCIONES DEL PAO POR LÍNEA ESTRATÉGICA.
     *
     * Funcionalidad: Obtiene las Acciones del PAO asociadas a una Línea
     * Estratégica específica. Ordena los resultados alfabéticamente por
     * descripción.
     *
     * @param idEstrategia Identificador de la Línea Estratégica a consultar.
     * @return Lista de {@link Plnacciondeta} asociadas a la Línea Estratégica
     * indicada.
     * @throws Exception Cuando ocurre un error durante la consulta de Acciones
     * del PAO.
     */
    @Override
    public List<Plnacciondeta> buscarAccionesPorEstrategia(BigInteger idEstrategia) throws Exception {
        try {
            Query q = em.createQuery("SELECT a FROM Plnacciondeta a WHERE a.idestrategia.idestrategia = :id ORDER BY a.descrip ASC");
            q.setParameter("id", idEstrategia);
            q.setHint("eclipselink.refresh", "true");
            return q.getResultList();
        } catch (Exception e) {
            return new ArrayList<Plnacciondeta>();
        }
    }

    /**
     * BUSCA ACCIONES DEL PAO POR PLAN ANUAL OPERATIVO.
     *
     * Funcionalidad: Obtiene todas las Acciones asociadas a un PAO, ordenadas
     * por identificador de acción.
     *
     * @param idPao Identificador del Plan Anual Operativo a consultar.
     * @return Lista de {@link Plnacciondeta} asociadas al PAO indicado.
     */
    @Override
    public List<Plnacciondeta> buscarAccionesPorPao(BigInteger idPao) {
        try {
            String sql = "SELECT a FROM Plnacciondeta a WHERE a.idpao.idpao = :idPao ORDER BY a.idaccionpao ASC";

            Query q = em.createQuery(sql);
            q.setParameter("idPao", idPao);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");

            return q.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Plnacciondeta>();
        }
    }

    @Override
    public boolean accionTieneEvaluaciones(BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT COUNT(e) FROM Plnaccioneval e WHERE e.idaccionpao.idaccionpao = :idAccion";
            Long count = (Long) em.createQuery(jpql)
                    .setParameter("idAccion", idAccion)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al validar existencia de evaluaciones para la Acción ID: " + idAccion, e);
            throw new Exception("Error al verificar evaluaciones: " + e.getMessage());
        }
    }

    @Override
    public boolean accionTieneSeguimientos(BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT COUNT(s) FROM Plnaccionseguimiento s WHERE s.idaccionpao.idaccionpao = :idAccion";
            Long count = (Long) em.createQuery(jpql)
                    .setParameter("idAccion", idAccion)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al validar de seguimientos para la Acción ID: " + idAccion, e);
            throw new Exception("Error al verificar seguimientos: " + e.getMessage());
        }
    }

    @Override
    public boolean accionEstaVinculadaAIniciativa(BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT COUNT(v) FROM Plnpaoinicdeta v WHERE v.idacciondeta.idaccionpao = :idAccion";
            Long count = (Long) em.createQuery(jpql)
                    .setParameter("idAccion", idAccion)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al validar existencia de vinculaciones a una iniciativa para la Acción ID: " + idAccion, e);
            throw new Exception("Error al verificar vínculos con iniciativas: " + e.getMessage());
        }
    }
    // ========================================================================
    // MÓDULO ACCIONES NO PLANIFICADAS
    // ========================================================================

    /**
     * BUSCA ACCIONES NO PLANIFICADAS POR PAO.
     *
     * Funcionalidad: Obtiene las Acciones No Planificadas asociadas a un Plan
     * Anual Operativo específico. Ordena los resultados por fecha de creación.
     *
     * @param idPao Identificador del Plan Anual Operativo a consultar.
     * @return Lista de {@link Plnaccionsinplan} asociadas al PAO indicado.
     * @throws Exception Cuando ocurre un error durante la consulta de Acciones
     * No Planificadas.
     */
    @Override
    public List<Plnaccionsinplan> buscarAccionesNoPlanificadas(BigInteger idPao) throws Exception {
        try {
            String jpql = "SELECT a FROM Plnaccionsinplan a WHERE a.idpao.idpao = :idPao ORDER BY a.fchacrea ASC";
            Query q = em.createQuery(jpql);
            q.setParameter("idPao", idPao);
            return q.getResultList();
        } catch (Exception e) {
            return new ArrayList<Plnaccionsinplan>();
        }
    }

    /**
     * BUSCA ACCIONES NO PLANIFICADAS POR PAO Y LÍNEA ESTRATÉGICA.
     *
     * Funcionalidad: Obtiene las Acciones No Planificadas asociadas a un Plan
     * Anual Operativo y a una Línea Estratégica específica. Ordena los
     * resultados por fecha de creación de forma descendente.
     *
     * @param idPao Identificador del Plan Anual Operativo.
     * @param idEstrategia Identificador de la Línea Estratégica.
     * @return Lista de {@link Plnaccionsinplan} que cumplen con los criterios
     * indicados.
     * @throws Exception Cuando ocurre un error durante la consulta de Acciones
     * No Planificadas.
     */
    @Override
    public List<Plnaccionsinplan> buscarNoPlanificadasPorEstrategia(BigInteger idPao, BigInteger idEstrategia) throws Exception {
        String jpql = "SELECT n FROM Plnaccionsinplan n WHERE n.idpao.idpao = :idPao AND n.idestrategia.idestrategia = :idEst ORDER BY n.fchacrea DESC";
        Query q = em.createQuery(jpql);
        q.setParameter("idPao", idPao);
        q.setParameter("idEst", idEstrategia);
        return q.getResultList();
    }

    // ========================================================================
    // MÓDULO PLAN TRIMESTRAL
    // ========================================================================
    /**
     * BUSCA DETALLES DE PLANIFICACIÓN TRIMESTRAL POR ACCIÓN DEL PAO.
     *
     * Funcionalidad: Obtiene los Detalles de Planificación Trimestral asociados
     * a una Acción específica del PAO. Ordena los resultados por trimestre.
     *
     * @param idAccion Identificador de la Acción del PAO a consultar.
     * @return Lista de {@link Plnaccidetplantrim} asociados a la Acción
     * indicada.
     * @throws Exception Cuando ocurre un error durante la consulta de detalles.
     */
    @Override
    public List<Plnaccidetplantrim> buscarDetallesPorAccion(BigInteger idAccion) throws Exception {
        if (idAccion == null) {
            return new ArrayList<Plnaccidetplantrim>();
        }
        String jpql = "SELECT d FROM Plnaccidetplantrim d WHERE d.idaccionpao.idaccionpao = :idAccion ORDER BY d.trimes ASC";
        Query q = em.createQuery(jpql);
        q.setParameter("idAccion", idAccion);
        return q.getResultList();
    }

    // ========================================================================
    // MÓDULO EVALUACIÓN
    // ========================================================================
    /**
     * BUSCA LA EVALUACIÓN ASOCIADA A UNA ACCIÓN DEL PAO.
     *
     * Funcionalidad: Obtiene la Evaluación vinculada a una Acción específica
     * del Plan Anual Operativo.
     *
     * @param idAccion Identificador de la Acción del PAO a consultar.
     * @return Objeto {@link Plnaccioneval} asociado a la Acción, o {@code null}
     * si no existe evaluación registrada.
     * @throws Exception Cuando ocurre un error durante la consulta de la
     * Evaluación.
     */
    @Override
    public Plnaccioneval buscarEvaluacionPorAccion(BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT e FROM Plnaccioneval e "
                    + "WHERE e.idaccionpao.idaccionpao = :id "
                    + "ORDER BY e.fchacrea DESC"; // ← la más reciente primero
            Query q = em.createQuery(jpql);
            q.setParameter("id", idAccion);
            q.setHint("eclipselink.refresh", "true");
            q.setMaxResults(1);
            List<Plnaccioneval> lista = q.getResultList();
            return lista.isEmpty() ? null : lista.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error buscando evaluación: " + e.getMessage());
        }
    }

    // En BusquedasPep.java
    @Override
    public List<Plnaccioneval> buscarEvaluacionesPorAccion(BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT e FROM Plnaccioneval e "
                    + "WHERE e.idaccionpao.idaccionpao = :id "
                    + "ORDER BY e.fchacrea ASC";
            Query q = em.createQuery(jpql);
            q.setParameter("id", idAccion);
            q.setHint("eclipselink.refresh", "true");
            return q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    /**
     * BUSCA UNA EVALUACIÓN POR ACCIÓN Y PERIODO ESPECÍFICO. Permite validar si
     * ya existe evaluación para ese periodo.
     */
    @Override
    public Plnaccioneval buscarEvaluacionPorAccionYPeriodo(
            BigInteger idAccion, String periodo) throws Exception {
        try {
            String jpql = "SELECT e FROM Plnaccioneval e "
                    + "WHERE e.idaccionpao.idaccionpao = :id "
                    + "AND e.perieval = :periodo";
            Query q = em.createQuery(jpql);
            q.setParameter("id", idAccion);
            q.setParameter("periodo", periodo);
            q.setHint("eclipselink.refresh", "true");
            return (Plnaccioneval) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // ========================================================================
    // MÓDULO SEGUIMIENTOS
    // ========================================================================
    /**
     * BUSCA EL SEGUIMIENTO ASOCIADO A UNA ACCIÓN DEL PAO.
     *
     * Funcionalidad: Obtiene el Seguimiento vinculado a una Acción específica
     * del Plan Anual Operativo.
     *
     * @param idAccion Identificador de la Acción del PAO a consultar.
     * @return Objeto {@link Plnaccionseguimiento} asociado a la Acción, o
     * {@code null} si no existe seguimiento registrado.
     * @throws Exception Cuando ocurre un error durante la consulta del
     * Seguimiento.
     */
    @Override
    public Plnaccionseguimiento buscarSeguimientoPorAccion(BigInteger idAccion) throws Exception {
        try {
            String jpql = "SELECT s FROM Plnaccionseguimiento s WHERE s.idaccionpao.idaccionpao = :idAccion";
            Query q = em.createQuery(jpql);
            q.setParameter("idAccion", idAccion);
            List<Plnaccionseguimiento> lista = q.getResultList();
            if (lista != null && !lista.isEmpty()) {
                return lista.get(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * OBTIENE EL HISTORIAL COMPLETO DE SEGUIMIENTOS DE UNA ACCIÓN.
     *
     * Funcionalidad: Obtiene todos los registros de seguimiento asociados a una
     * Acción del PAO, ordenados por fecha de creación descendente (más
     * recientes primero).
     *
     * @param idAccion Identificador de la Acción del PAO a consultar.
     * @return Lista de {@link Plnaccionseguimiento} asociados a la Acción.
     */
    @Override
    public List<Plnaccionseguimiento> obtenerHistorialSeguimientos(BigInteger idAccion) {
        try {
            String sql = "SELECT s FROM Plnaccionseguimiento "
                    + "s WHERE s.idaccionpao.idaccionpao = :idAccion "
                    + "ORDER BY s.fchacrea DESC";

            Query q = em.createQuery(sql);
            q.setParameter("idAccion", idAccion);

            return q.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Plnaccionseguimiento>();
        }
    }
    
    @Override
    public List<Plnaccionseguimiento> buscarSeguimientosPorPao(BigInteger idPao) throws Exception {
    String jpql = "SELECT s FROM Plnaccionseguimiento s " +
                  "JOIN s.idaccionpao a " +
                  "WHERE a.idpao.idpao = :idPao " +
                  "ORDER BY s.fchacrea DESC";
    
    return em.createQuery(jpql, Plnaccionseguimiento.class)
             .setParameter("idPao", idPao)
             .getResultList();
}

    // ========================================================================
    // MÓDULOS TRANSVERSALES - EMPLEADOS
    // ========================================================================
    /**
     * BUSCA EMPLEADOS ACTIVOS.
     *
     * Funcionalidad: Obtiene la lista de empleados con estado activo para su
     * selección y uso en procesos del sistema. Ordena los resultados por
     * nombre.
     *
     * @return Lista de {@link Rhuempleado} con estado activo.
     * @throws Exception Cuando ocurre un error durante la consulta de
     * empleados.
     */
    @Override
    public List<Rhuempleado> buscarEmpleadosActivos() throws Exception {
        try {
            String jpql = "SELECT e FROM Rhuempleado e "
                    + "WHERE e.estado = 1 "
                    + "ORDER BY e.persona.nombre1 ASC";

            Query q = em.createQuery(jpql);
            q.setHint("eclipselink.refresh", "true");
            return q.getResultList();
        } catch (NoResultException nre) {
            return new ArrayList<Rhuempleado>();
        } catch (Exception ex) {
            throw new Exception("Error buscando empleados: " + ex.getMessage());
        }
    }

    /**
     * BUSCA UN EMPLEADO POR IDENTIFICADOR.
     *
     * Funcionalidad: Obtiene un empleado específico a partir de su
     * identificador único.
     *
     * @param codemp Identificador del empleado a consultar.
     * @return Objeto {@link Rhuempleado} si existe, o {@code null} si no se
     * encuentra.
     * @throws Exception Cuando ocurre un error durante la consulta del
     * empleado.
     */
    @Override
    public Rhuempleado buscarEmpleadoPorId(BigInteger codemp) throws Exception {
        try {
            if (codemp == null) {
                return null;
            }
            return em.find(Rhuempleado.class, codemp);
        } catch (Exception ex) {
            return null;
        }
    }

    // ========================================================================
    // MÓDULOS TRANSVERSALES - CONSULTAS NATIVAS
    // ========================================================================
    /**
     * EJECUTA UNA CONSULTA SQL NATIVA.
     *
     * Funcionalidad: Permite ejecutar consultas directas a la base de datos
     * (Vistas o Queries complejos) que retornan arreglos de objetos, útil para
     * reportes o árboles jerárquicos planos.
     *
     * @param sql La sentencia SQL nativa.
     * @param parametro El parámetro principal (usualmente el ID) para el filtro
     * ?1.
     * @return Lista de arreglos de objetos (filas y columnas).
     * @throws Exception Cuando ocurre un error durante la ejecución de la
     * consulta.
     */
    @Override
    public List<Object[]> ejecutarQueryNativo(String sql, Object parametro) throws Exception {
        try {
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, parametro);
            return q.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al ejecutar Vista SQL ", e);
            return new ArrayList<>();
        }
    }

    /**
     * OBTIENE COMPLETO UN PEP DESDE LA VISTA VWDETALLESPEP. Ejecuta la consulta
     * nativa y retorna las filas crudas.
     *
     * @param idPep Identificador del PEP a consultar.
     * @return Lista de Object[] con las columnas de VWDETALLESPEP.
     */
    @Override
    public List<Object[]> obtenerDetallesPep(BigInteger idPep) {
        try {
            String sql = "SELECT * FROM VWDETALLESPEP "
                    + "WHERE IDPEP = ?1 "
                    + "ORDER BY IDPERSPECTIVA, CODIGOOE, CODIGOINDICADOR";
            return ejecutarQueryNativo(sql, idPep);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al ejecutar Vista SQL", e);
            return new ArrayList<Object[]>();
        }
    }

    /**
     * BUSCA UN DETALLE TRIMESTRAL POR SU ID DIRECTAMENTE EN BD. Se usa para
     * verificar el valor real guardado sin depender del estado en memoria.
     */
    public Plnaccidetplantrim buscarDetalleTrimPorId(BigInteger idDetalle) {
        try {
            if (idDetalle == null) {
                return null;
            }
            return em.find(Plnaccidetplantrim.class, idDetalle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * Retorna las acciones vinculadas a una iniciativa FILTRADAS por el PAO
     * activo. Evita que desde el PAO 2 se vean o desvinculen acciones que
     * pertenecen al PAO 1.
     */
    public List<Plnacciondeta> buscarAccionesVinculadasPorIniciativaYPao(
            BigInteger idIniciativa, BigInteger idPao) {

        try {
            if (idIniciativa == null || idPao == null) {
                return new ArrayList<>();
            }

            return em.createQuery(
                    "SELECT d.idacciondeta FROM Plnpaoinicdeta d "
                    + "WHERE d.idiniciativa.idiniciativa = :idInic "
                    + "AND d.idacciondeta.idpao.idpao = :idPao "
                    + "ORDER BY d.idacciondeta.idaccionpao",
                    Plnacciondeta.class)
                    .setParameter("idInic", idIniciativa)
                    .setParameter("idPao", idPao)
                    .getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}