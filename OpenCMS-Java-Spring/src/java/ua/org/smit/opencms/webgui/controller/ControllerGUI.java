/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.org.smit.opencms.webgui.controller;

import java.util.ArrayList;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ua.org.smit.opencms.authorization.AuthService;
import ua.org.smit.opencms.authorization.AuthServiceImpl;
import ua.org.smit.opencms.authorization.UserAuth;
import ua.org.smit.opencms.content.dao.MaterialEntityCMS;
import ua.org.smit.opencms.content.LogicInterfaceImpl;
import ua.org.smit.opencms.content.MaterialDto;
import ua.org.smit.opencms.content.LogicInterface;
import ua.org.smit.opencms.webgui.dto.ConverterGuiToLogic;
import ua.org.smit.opencms.webgui.dto.MaterialGUIDto;
import ua.org.smit.opencms.webgui.utils.SessionUtil;


@Controller
public class ControllerGUI {
    
    private final AuthService authService = new AuthServiceImpl();
    private LogicInterface logic = new LogicInterfaceImpl();
    private ConverterGuiToLogic convertor = new ConverterGuiToLogic();
    private final TextUtil textUtil = new TextUtil();
    
    @RequestMapping(value= {"/","/home"})
    public ModelAndView getHome(HttpServletRequest request){
        ModelAndView model = new ModelAndView("home");
        
        ArrayList<MaterialEntityCMS> materialsCat1 = logic.getLastPublicMaterialsByCatAndLimit(1, 5);
        ArrayList<MaterialEntityCMS> materialsCat2 = logic.getLastPublicMaterialsByCatAndLimit(2, 5);
        ArrayList<MaterialEntityCMS> materialsCat3 = logic.getLastPublicMaterialsByCatAndLimit(3, 5);
        
        model.addObject("materialsCat1", materialsCat1);
        model.addObject("materialsCat2", materialsCat2);
        model.addObject("materialsCat3", materialsCat3);
        model.addObject("textUtil", textUtil);
        
        
        
        UserAuth user = authService.getUserBySession(SessionUtil.getSession(request));
        if (!user.isGuest()){
            System.out.println("Hello " + user.getLogin());
        } else {
            System.out.println("You are Guest...");
        }
        
        return model;
    }
    
    @RequestMapping(value= "/material_manager")
    public Object materialManager(
            HttpServletRequest request,
            @RequestParam(value = "alias", required = false) String alias){
        
        UserAuth user = authService.getUserBySession(SessionUtil.getSession(request));
        if (user.isGuest()) return "redirect:login";
        if (!user.isAdminGroup()) return "redirect:login";
        
        ModelAndView model = new ModelAndView("material_manager");
        if(alias != null){
            MaterialEntityCMS material = logic.getMaterialByAlias(alias);
            model.addObject("material", material);
        }
        model.addObject("categories", logic.getAllCategories());
        return model;
    }
    
    
    @RequestMapping(value= "/material_manager_action")
    public Object materialManagerAction(MaterialGUIDto material,
            HttpServletRequest request){
        
        UserAuth user = authService.getUserBySession(SessionUtil.getSession(request));
        if (user.isGuest()) return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        
        String alias = null;
        
        if (material.getId()>0){
            MaterialDto dtoLogic = this.convertor.convert(material);
            this.logic.updateMaterial(dtoLogic);
            MaterialEntityCMS entity = logic.getMaterialByID(material.getId());
            alias = entity.getAlias();
            
        } else {            
            if (logic.thisCategoryExist(material.getCategoryId())){
                MaterialDto dtoLogic = this.convertor.convert(material);
                int id = this.logic.makeMaterial(dtoLogic);
                MaterialEntityCMS entity = logic.getMaterialByID(id);
                alias = entity.getAlias();
            } else {
                // err!
            }
        }
        

        return "redirect:material_manager?alias=" + alias;
    }
    
    @RequestMapping(value = "/categories")
    public ModelAndView categories(){
        ModelAndView model = new ModelAndView("categories");
        model.addObject("categories", logic.getAllCategories());
        return model;
    }
    
    @RequestMapping(value= "/show_material")
    public ModelAndView showMaterial(
            @RequestParam(value = "alias") String alias){
        
        ModelAndView model = new ModelAndView("materialView");
        MaterialEntityCMS material = logic.getMaterialByAlias(alias);
        model.addObject("material", material);
        model.addObject("textUtil", textUtil);

        return model;
    }
    
    @RequestMapping(value= "/all_materials")
    public ModelAndView getAllmaterials(){
        ModelAndView model = new ModelAndView("all_materials");
        ArrayList<MaterialEntityCMS> all_materials = logic.getAll();
        model.addObject("all_materials", all_materials);    
        model.addObject("textUtil", textUtil);
        
        return model;
    }
    
    
    @RequestMapping(value= "/set_material_public")
    public Object PublicMaterial(@RequestParam(value = "alias") String alias){

       logic.makeThisMaterialAsPublic(alias);

        return "redirect:show_material?alias=" + alias;
    }
    
    @RequestMapping(value= "/set_material_no_public")
    public Object NoPublicMaterial(@RequestParam(value = "alias") String alias){
       logic.makeThisMaterialAsNoPublic(alias);

        return "redirect:show_material?alias=" + alias;
    }
    
    @RequestMapping(value = "/change_public_status_material")
    public Object changePublicStatusMaterial(
            @RequestParam(value = "alias") String alias,
            @RequestParam(value = "public") String publicS){
        
        // 1 guest / admin
        // 2 rules 
        // 3 
        
        boolean status = Boolean.valueOf(publicS);
        if (status){
            logic.makeThisMaterialAsPublic(alias);
        } else {
            logic.makeThisMaterialAsNoPublic(alias);
        }
        
        return "redirect:material_manager?alias=" + alias;
    }
    
    @RequestMapping(value = "/delete_material")
    public Object deleteMaterial(@RequestParam (value = "idMaterial") int id){
        
        logic.deleteByID(id);
        return "redirect:home";
    }
    
    @RequestMapping(value= "/category_materials")
    public ModelAndView getMaterialsByCategory(@RequestParam(value = "catId") int catId){
        ModelAndView model = new ModelAndView("category_materials");
        ArrayList<MaterialEntityCMS> category_materials = logic.getMaterialsByCategory(catId);
        model.addObject("category_materials", category_materials);    
        model.addObject("textUtil", textUtil);
        
        return model;
    }
    
    
}
