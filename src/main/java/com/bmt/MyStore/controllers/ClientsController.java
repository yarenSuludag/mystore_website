package com.bmt.MyStore.controllers;


import com.bmt.MyStore.models.Client;
import com.bmt.MyStore.models.ClientDto;
import com.bmt.MyStore.repositories.ClientsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientsController {

    @Autowired
    private ClientsRepository repo;

    @GetMapping
    public String getClients(Model model){
        List<Client> clients = repo.getClients();
        model.addAttribute("clients", clients);
        return "clients/index";

    }

   @GetMapping("/create")
    public String showCreatePage(Model model) {
       ClientDto clientDto = new ClientDto();
       model.addAttribute("clientDto", clientDto);
       return "clients/create";

   }

   @PostMapping("/create")
    public String createClient(
            @Valid @ModelAttribute ClientDto clientDto,
            BindingResult result

   ) {

        if(repo.getClient(clientDto.getEmail()) != null){
            result.addError(
                    new FieldError("clientDto", "email", clientDto.getEmail()
                    , false, null, null, "Email address is already used")

            );


        }

        if(result.hasErrors()) {
            return "clients/create";
        }

        Client client = new Client();
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        client.setEmail(clientDto.getEmail());
        client.setPhone(clientDto.getPhone());
        client.setAddress(clientDto.getAddress());
        client.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));


        repo.createClient(client);


        return  "redirect:/clients";

   }

   @GetMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id
           )
   {
       Client client = repo.getClient(id);
       if(client==null){
           return "redirect:/clients";
       }

       model.addAttribute("client", client);

       ClientDto clientDto = new ClientDto();
       clientDto.setFirstName(client.getFirstName());
       clientDto.setLastName(client.getLastName());
       clientDto.setEmail(client.getEmail());
       clientDto.setPhone(client.getPhone());
       clientDto.setAddress(client.getAddress());

       model.addAttribute("clientDto", clientDto);

       return "clients/edit";
   }


@PostMapping("/edit")
    public String updateClient(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ClientDto clientDto,
            BindingResult result
) {

        Client client = repo.getClient(id);
        if( client==null) {
            return "redirect:/clients";
        }


        model.addAttribute("client", client);
        if(result.hasErrors()) {
            return "clients/edit";
        }

        // update client details
    client.setFirstName(clientDto.getFirstName());
    client.setLastName(clientDto.getLastName());
    client.setEmail(clientDto.getEmail());
    client.setPhone(clientDto.getPhone());
    client.setAddress(clientDto.getAddress());

    repo.updateClient(client);

    return "redirect:/clients";
}

@GetMapping("/delete")
    public String deleteClient(
            @RequestParam int id )
{
    repo.deleteClient(id);

    return "redirect:/clients";
  }


    @GetMapping("/filter")
    public String getClients(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            Model model) {

        List<Client> clients = repo.getClientsFiltered(name, email, phone);
        model.addAttribute("clients", clients);
        return "clients/index";
    }

    @GetMapping("/search")
    public String getClientsBySearch(
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Client> clients = search != null && !search.isEmpty() ? repo.searchClients(search) : repo.getClients();
        model.addAttribute("clients", clients);
        return "clients/index";
    }

    @GetMapping("/sort")
    public String getClientsBySort(
            @RequestParam(value = "sort", required = false) String sort,
            Model model) {

        List<Client> clients;
        if ("name_asc".equals(sort)) {
            clients = repo.getClientsSortedByNameAsc();
        } else if ("name_desc".equals(sort)) {
            clients = repo.getClientsSortedByNameDesc();
        } else if ("phone_asc".equals(sort)) {
            clients = repo.getClientsSortedByPhoneAsc();
        } else if ("phone_desc".equals(sort)) {
            clients = repo.getClientsSortedByPhoneDesc();
        } else {
            clients = repo.getClients();
        }

        model.addAttribute("clients", clients);
        return "clients/index";
    }





}
