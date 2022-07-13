package trans.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.standard.expression.Each;
import trans.Driver;
import trans.data.DriverRepository;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/driver", produces = "application/json")
@CrossOrigin(origins = "*")
public class DriverController {
    private DriverRepository driverRepo;

    @Autowired
    public DriverController(DriverRepository driverRepo) {
        this.driverRepo = driverRepo;
    }

    @GetMapping("/list")
    public Iterable<Driver> listDriver() {
        return driverRepo.findAll();
    }

    //    @GetMapping("/{id}")
//    public ResponseEntity<Driver> driverById(@PathVariable("id") String id) {
//        Optional<Driver> optDriver = driverRepo.findById(id);
//        if (optDriver.isPresent()) {
//            return ResponseEntity.ok(optDriver.get());
//        }
//        return null;
//    }
    public static <T> void each(List<T> list, Each<T> each) throws RuntimeException {

        if (list == null) return;

        for (int index = 0; index < list.size(); index++)
            each.do_(index, list.get(index));
    }

    public interface Each<T> {

        void do_(int index, T item) throws RuntimeException;
    }

    @GetMapping("/search")
    public List<Driver> driversByType(@RequestParam(value = "type") String[] type) {
        String string = "Kinh Doanh,Cong Nghe,Ky Thuat";
        String[] strings = string.split(",");
        List<String> list = new ArrayList<>();
        for (String s : strings) {
            list.add(s);
        }
        each(list,(index, item) -> System.out.println(item));
        List<Driver> driverList = new ArrayList<>();
        List<Driver> drivers = new ArrayList<>();
        driverRepo.findAll().forEach(i -> driverList.add(i));
        Arrays.stream(type).forEach(x -> searchByType(driverList, x).forEach(i -> drivers.add(i)));
        return drivers;
    }

    @GetMapping("/search/{id}")
    public List<Driver> searchByKey(@PathVariable("id") String key) {
        List<Driver> drivers = new ArrayList<>();
        driverRepo.findAll().forEach(i -> drivers.add(i));
        return filterByKey(drivers, key);
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> postDriver(@Valid @RequestBody Driver driver) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            Optional<Driver> validDriver = driverRepo.findById(driver.getIdCard());
            if (validDriver.isPresent()) {
                response.put("defaultMessage", "Số CMND " + driver.getIdCard() + " đã tồn tại ");
                response.put("field", false);
                return response;
            } else {
                driverRepo.save(driver);
                return response;
            }
        } catch (Exception e) {
            return response;
        }
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateDriver(@PathVariable("id") String id, @Valid @RequestBody Driver driver) {
        HashMap<String, Object> response = new HashMap<>();
        try {
            if (!driver.getIdCard().equals(id)) {
                throw new IllegalStateException("Error");
            } else {
                driverRepo.save(driver);
                return response;
            }
        } catch (Exception e) {
            return response;
        }
    }

    @DeleteMapping("{id}")
    public void deleteDriver(@PathVariable("id") String id) {
        driverRepo.deleteById(id);
    }

    @GetMapping(value = "/page/_pageNo={pageNo}&_limit={pageSize}")
    public List<Driver> findPaginated(@PathVariable("pageNo") int pageNo, @PathVariable("pageSize") int pageSize) {
        Sort sort = "asc".equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by("idCard").ascending() :
                Sort.by("idCard").descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        return driverRepo.findAll(pageable).getContent();
    }

    private List<Driver> searchByType(List<Driver> drivers, String type) {
        return drivers
                .stream()
                .filter(x -> x.getType().toLowerCase().contains(type.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Driver> filterByKey(
            List<Driver> drivers, String key) {
        return drivers
                .stream()
                .filter(x -> x.getIdCard().toLowerCase().contains(key.toLowerCase()))
                .collect(Collectors.toList());
    }
}
